package com.parkhanee.tinychat;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

/**
 * 싱글톤으로 구현해야 함    -->   서비스 에서만 새로 객체 생성하고 채팅액티비티의 async에서 객체를 받음.
 * MyTcpAsync는 메세지 전송 누를 때 마다 생성되는 쓰레드 이고
 * MyTcpClient 는 계속 객체 하나만, 소켓연결 하나만 실행,유지하면서 액티비티-->async-->tcpClient 로 받은 요청 처리.
 * */
public class MyTCPClient {
    public static MyTCPClient instance = null;
    private static final String TAG = "MyTCPClient";
    private final Handler handler;
    private BufferedReader in;
    private PrintWriter out;
    private boolean run = false;
    private String server_ip, server_port, id;
    private Socket socket;

    // 연결 상태
    static final int CONNECTING = 1; // 서버와 소켓 연결 시도 중
    static final int CONNECTED = 0; // 서버와 소켓 연결 완료
    static final int SHUTDOWN = 5;
    static final int ERROR = 6;
    static final int READY_TO_TALK = 7; // 사용자 아이디 식별 완료
    // 메세지
    static final int SENDING = 2; // 서버로 메세지 보내는 중
    static final int SENT = 3; // 내가 보냈던 메세지 돌려받음
    static final int RECEIVED = 4;  // 서버로부터 다른 유저가 보낸 채팅 메세지 받음
    static final int INFO = 8; // 서버로부터 알림 메세지 받음


    // 서버에 tcp 주고받을 때  json 형식 이름
    public static final String JSON_ID = "id";
    public static final String JSON_MSG = "msg";
    public static final String JSON_INFO = "info";


    /**
     * TCPClient class constructor, which is created in AsyncTasks after the button click.
     * @param handler Handler passed as an argument for updating the UI with sent messages
     * @param strings server ip, server tcp port, user id, room id
     */
    public MyTCPClient(Handler handler, String... strings){
        Log.d(TAG, "MyTCPClient: constructor");
        this.handler = handler;
        server_ip = strings[0];
        server_port = strings[1];
        id = strings[2];
    }

    public static synchronized MyTCPClient getInstance(Handler handler, String... strings){
        if (instance==null){
            Log.d(TAG, "getInstance: return NEW instance");
            instance = new MyTCPClient(handler, strings);
        } else {
            Log.d(TAG, "getInstance: return existing instance");
        }
        return instance;
    }

    public void run() {

        if (run){
            Log.d(TAG, "run: already running ㅠㅠㅠ ");
            return;
        }

        run = true;

        try {

            InetAddress serverAddr = InetAddress.getByName(server_ip);

            Log.d(TAG, "run: connecting . . .");

            handler.sendEmptyMessage(CONNECTING);

            socket = new Socket(serverAddr, Integer.parseInt(server_port));

            try {
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d(TAG, "run: in/out created");
                handler.sendEmptyMessage(CONNECTED);

                while (run){
                    String incomingMessage = in.readLine();

                    /**
                     * Incoming message is passed to MessageCallback object.
                     * Next it is retrieved by AsyncTask and passed to onPublishProgress method.
                     *
                     */
                    // todo jsonObject 확인해서 MSG 이면 Async에 넘기고, INFO이면 여기서 처리.
                    if (incomingMessage !=null){
                        List<String> result = MyUtil.readJSONObject(incomingMessage);
                        if (result != null){
                            if (result.get(0).equals(JSON_MSG)){ // 메세지 받은 경우

                                // rid, id, body listener대신 handler에게 넘기기
//                                listener.callbackMessageReceiver(result.get(1),result.get(2),result.get(3));
                                Message m = new Message();
                                m.obj = result.get(2) +" : "+ result.get(3) ;
                                m.what = RECEIVED;
                                handler.sendMessage(m);

                            } else if (result.get(0).equals(JSON_INFO)){

                                switch (result.get(1)){
                                    case "SUBMIT USER ID":
                                        sendMessage(JSON_ID,"",id);
                                        break;
                                    case "READY TO TALK":
                                        // 첫번째로 보내는 메세지
//                                        sendMessage(JSON_MSG,firstRid,firstMessage);
                                        handler.sendEmptyMessage(READY_TO_TALK);
                                        break;
                                    default:
                                        // 다른 info 메세지 온 경우
                                        break;
                                }
                            } else {
                                // json에 msg, info object 가 없었던 경우
                            }
                        }

                        Log.d(TAG, "run : received message : "+ incomingMessage);
                    }


                }

                // 무한루프 while 에서 종료하는 것으로 try 블럭을 벗어나기 때문에,
                // 이 부분 또는 catch 아래의 finally 블럭은 실행되지 않는다.

            } catch (IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(ERROR);
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            Log.d(TAG, "run : 서버와 연결 실패");
            handler.sendEmptyMessage(ERROR);
        }

    }

    // 서버에 메세지 전송
    public void sendMessage(String type,String rid, String message){
        if (out != null && !out.checkError()){
            try {
                JSONObject object = new JSONObject();
                switch (type){
                    case JSON_ID:
                        object.put(JSON_ID,message);
                        break;
                    case JSON_MSG:
                        JSONObject msgObject = new JSONObject();
                        msgObject.put("client","true");
                        msgObject.put("rid",rid);
                        msgObject.put("body",message);
                        object.put(JSON_MSG,msgObject);
                        break;
                    case JSON_INFO:
                        object.put(JSON_INFO,message);
                        break;
                }
                message = object.toString();


                out.println(message);
                out.flush(); // TODO: 2017. 8. 19. flush ?

                Message msg = new Message();
                msg.obj = message;
                msg.what = SENDING;
                handler.sendMessage(msg);
                Log.d(TAG, "sendMessage: "+message);

            } catch (JSONException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = ERROR;
                msg.obj = "fail to send message due to json exception";
                handler.sendMessage(msg);
                Log.d(TAG, "sendMessage: fail to send message due to json exception");
            }


        } else {
            Message msg = new Message();
            msg.what = ERROR;
            msg.obj = "fail to send message due to printwriter error";
            handler.sendMessage(msg);
            Log.d(TAG, "sendMessage: fail to send message due to printwriter error");
        }
    }

    public void stopClient(){

        if (run){
            run = false;
            try {
                out.flush();
                out.close();
                in.close();
                socket.close();
                handler.sendEmptyMessage(SHUTDOWN);
                Log.d(TAG, "stopClient : socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
            instance = null;
            Log.d(TAG, "stopClient: OK");
        }else{
            Log.d(TAG, "stopClient: tcp client is NOT running, thus not stopping");
            instance = null;
        }


    }

    public boolean isRunning(){
        return run;
    }


} //MyTCPClient

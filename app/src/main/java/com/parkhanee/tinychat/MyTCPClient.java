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

/**
 * 싱글톤으로 구현해야 함
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
    private MessageCallback listener = null;
    private String server_ip, server_port, id;
    private Socket socket;

    // 서버에 tcp 주고받을 때  json 형식 이름
    public static final String ID = "id";
    public static final String MSG = "msg";
    public static final String INFO = "info";


    /**
     * TCPClient class constructor, which is created in AsyncTasks after the button click.
     * @param handler Handler passed as an argument for updating the UI with sent messages
     * @param listener Callback interface object
     * @param strings server ip, server tcp port, user id, room id
     */
    public MyTCPClient(Handler handler, MessageCallback listener, String... strings){
        Log.d(TAG, "MyTCPClient: constructor");
        this.listener = listener;
        this.handler = handler;
        server_ip = strings[0];
        server_port = strings[1];
        id = strings[2];
    }

    public static synchronized MyTCPClient getInstance(Handler handler, MessageCallback listener, String... strings){
        if (instance==null){
            Log.d(TAG, "getInstance: return NEW instance");
            instance = new MyTCPClient(handler, listener, strings);
        } else {
            Log.d(TAG, "getInstance: return existing instance");
        }
        return instance;
    }

    public void run(String firstRid, String firstMessage) {

        if (run){
            Log.d(TAG, "run: already running ㅠㅠㅠ ");
            return;
        }

        run = true;

        try {

            InetAddress serverAddr = InetAddress.getByName(server_ip);

            Log.d(TAG, "run: connecting . . .");

            handler.sendEmptyMessage(ChatActivity.CONNECTING);

            socket = new Socket(serverAddr, Integer.parseInt(server_port));

            try {
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d(TAG, "run: in/out created");
                handler.sendEmptyMessage(ChatActivity.CONNECTED);

                while (run){
                    String incomingMessage = in.readLine();

                    if (incomingMessage !=null && listener !=null){
                        /**
                         * Incoming message is passed to MessageCallback object.
                         * Next it is retrieved by AsyncTask and passed to onPublishProgress method.
                         *
                         */
                        switch (incomingMessage){

//                            case "SUBMIT ROOM":
//                                // TODO: 2017. 8. 22. 한 tcp연결에 방이 정해저있는게 아니라 메세지 보낼 때 마다 방정보 알려주어야 . .  (아니면 방마다 tcp 연결 ?)
//                                sendMessage(rid);
//                                break;
//                            case "NEW ROOM CREATED" :
//                                break;
                            case "SUBMIT USER ID":
                                sendMessage(ID,"",id);
                                break;
                            case "NEW USER ID ACCEPTED" :
                                break;
                            case "USER ID ACKNOWLEDGED" :
                                break;
//                            case "ENTER THE ROOM" :
//                                break;
                            case "READY TO TALK": // 채팅방, 이름 식별 끝남
                                handler.sendEmptyMessage(ChatActivity.READY_TO_TALK);
                                sendMessage(MSG,firstRid,firstMessage);
                                break;
                            default: // 채팅 메세지 받은 경우에만 asyncTask로 넘김
                                listener.callbackMessageReceiver(incomingMessage);
                                break;
                        }


                        Log.d(TAG, "run: received message : "+ incomingMessage);
//                        incomingMessage = null;
                    }
                }

                Log.d(TAG, "run: false"); // FIXME: 2017. 8. 19. 여기도 안오는 것 같은데 ?


            } catch (IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(ChatActivity.ERROR);
            }




        } catch (IOException e1) {
            e1.printStackTrace();
            Log.d(TAG, "run: 서버와 연결 실패");
            handler.sendEmptyMessage(ChatActivity.ERROR);
        }

    }

    // 서버에 메세지 전송
    public void sendMessage(String type,String rid, String message){
        if (out != null && !out.checkError()){
            try {
                JSONObject object = new JSONObject();
                switch (type){
                    case ID :
                        object.put(ID,message);
                        break;
                    case MSG :
                        JSONObject msgObject = new JSONObject();
                        msgObject.put("rid",rid);
                        msgObject.put("body",message);
                        object.put(MSG,msgObject);
                        break;
                    case INFO :
                        object.put(INFO,message);
                        break;
                }
                message = object.toString();


                out.println(message);
                out.flush(); // TODO: 2017. 8. 19. flush ?

                Message msg = new Message();
                msg.obj = message;
                msg.what = ChatActivity.SENT;
                handler.sendMessage(msg);
                Log.d(TAG, "sendMessage: "+message);

            } catch (JSONException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = ChatActivity.ERROR;
                msg.obj = "fail to send message due to json exception";
                handler.sendMessage(msg);
                Log.d(TAG, "sendMessage: fail to send message due to json exception");
            }


        } else {
            Message msg = new Message();
            msg.what = ChatActivity.ERROR;
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
                handler.sendEmptyMessage(ChatActivity.SHUTDOWN); // TODO: 2017. 8. 19.  sent 가 아니라 shutdown ?
                Log.d(TAG, "run: socket closed");
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

    public interface MessageCallback {
        /**
         * Method overriden in AsyncTask 'doInBackground' method while creating the MyTCPClient object.
         * @param message Received message from server app.
         */
        void callbackMessageReceiver(String message);
    }

} //MyTCPClient

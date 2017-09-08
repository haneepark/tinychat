package com.parkhanee.tinychat;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parkhanee.tinychat.classbox.Chat;

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
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

     // Handler Flag
        // 연결 상태
        static final int CONNECTING = 1; // 서버와 소켓 연결 시도 중
        static final int CONNECTED = 0; // 서버와 소켓 연결 완료
        static final int SHUTDOWN = 5;
        static final int CONNECTION_ERROR = 6;
        static final int READY_TO_TALK = 7; // 사용자 아이디 식별 완료
        // 메세지
        static final int SENT = 3; // 서버로부터 내가 보낸 메세지 돌려받아서 제대로 보낸것까지 다 확인 됨.  // 항상 msg obj의 타입은 List<String> !!
        static final int SEDNING = 10; // 서버로 메세지를 오류없이 보냄
        static final int RECEIVED = 4;  // 서버로부터 다른 유저가 보낸 채팅 메세지 받음   // 항상 msg obj의 타입은 List<String> !!
        static final int INFO = 8; // 서버로부터 알림 메세지 받음
        static final int NEW_ROOM = 9; // 서버로부터 알림 메세지 받음
        static final int MSG_ERROR = 11;

    // 서버에 tcp 주고받을 때  json 형식 이름
    private static final String JSON_ID = "id";
    static final String JSON_MSG = "msg";
    static final String JSON_INFO = "info";
    static final String JSON_REQUEST = "request"; // 클라에서 서버에게 요청. e.g.단체방 생성
    static final String JSON_NEW_ROOM = "ppl";

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
        instance = this;
    }

    @Nullable
    public static synchronized MyTCPClient getInstance(){
        if (instance==null){
            Log.d(TAG, "getInstance: is null");
            return null;
        } else {
            Log.d(TAG, "getInstance: return existing instance");
            return instance;
        }

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

            // 서버 실행해 놓지 않으면 java.net.ConnectException: Connection refused
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
                     * Incoming message is passed to Handler.
                     * Then it is retrieved by MyTCPService and passed to ChatActivity, RoomTab, or Notification via NewMessageCallbackListener.
                     */
                    // jsonObject 확인해서 MSG 이면 Async에 넘기고, INFO이면 여기서 처리.
                    if (incomingMessage !=null){
                        Log.d(TAG, "run: incomingMessage != null");
                        List<String> result = MyUtil.readJSONObject(incomingMessage);
                        if (result != null){
                            if (result.get(0).equals(JSON_MSG)){ // 메세지 받은 경우

                                Message m = new Message();
                                m.obj = result;
                                if (result.get(2).equals(id)){ // 내가 보냈던 메세지
                                    m.what = SENT;
                                }else { // 받은 메세지
                                    m.what = RECEIVED;
                                }
                                handler.sendMessage(m);

                            } else if(result.get(0).equals(JSON_NEW_ROOM)){ // 새 방 정보와 함께 메세지 받은 경우 (내가 보냈던 거일수도 있음.)
                                Message m = new Message();
                                m.obj = result;
                                m.what = NEW_ROOM;
                                handler.sendMessage(m);
                            } else if (result.get(0).equals(JSON_INFO)){

                                Log.d(TAG, "run: json_info");

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
                handler.sendEmptyMessage(CONNECTION_ERROR);
                stopClient();
                // TODO: 2017. 8. 31. run 실행중에 인터넷 끊기면  in.readline에서 exception 나와서 여기로 옴.
                // 그런데 서버에서 사용자 나가는 것을 감지하지 못함. 상관없나.. ?
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            Log.d(TAG, "run : 서버와 연결 실패");
            handler.sendEmptyMessage(CONNECTION_ERROR);
        }

    }

    // 서버에 메세지 전송
    public void sendMessage(String type,String rid, String message){
        Date date = new Date();
        long unixTime = date.getTime()/1000;
        int randomNum = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            randomNum = ThreadLocalRandom.
                    current().nextInt(0, 1000 + 1); // generage random within 10 to 100
        }
        String mid = String.valueOf(randomNum)+rid+unixTime;

        if (out != null && !out.checkError()){
            try {
                JSONObject object = new JSONObject();
                switch (type){
                    case JSON_ID:
                        object.put(JSON_ID,message);
                        break;
                    case JSON_MSG:
                        JSONObject msgObject = new JSONObject();

                        Log.d(TAG, "sendMessage: unitTime " +unixTime );
                        msgObject.put("unixTime",String.valueOf(unixTime));
                        msgObject.put("rid",rid);
                        msgObject.put("body",message);
                        msgObject.put("id",id); // 아래에 핸들러에게 보낼 때 경우 때문에 넣음.
                        msgObject.put("mid",mid);

                        object.put(JSON_MSG,msgObject);

                        break;
                    case JSON_INFO:
                        object.put(JSON_INFO,message);
                        break;
                    case JSON_REQUEST:
                        // type : json type , rid : 단체방 사람수와 아이디 목록, message : 처음 보내고자하는 메세지

                        JSONObject requestObject = new JSONObject();
                        requestObject.put("unixTime",String.valueOf(unixTime));
                        requestObject.put(JSON_NEW_ROOM,rid); // "ppl" : "3:68620823,11111111,22222222"
                        requestObject.put("body",message);
                        requestObject.put("id",id); // 아래에 핸들러에게 보낼 때 경우 때문에 넣음.
                        requestObject.put("mid",mid);
                        object.put(JSON_REQUEST,requestObject);
                        break;
                }
                message = object.toString();

                out.println(message);
                out.flush(); // TODO: 2017. 8. 19. flush ?

                // 채팅 메세지 전송이 완료 된 것을 서비스의 핸들러에게 알림
                if (type.equals(JSON_MSG)){
                    Message msg = new Message();
                    msg.obj = MyUtil.readJSONObject(message);
                    msg.what = SEDNING;
                    handler.sendMessage(msg);
                }

                Log.d(TAG, "sendMessage: "+message);

            } catch (JSONException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = MSG_ERROR;
                msg.obj = mid;
                handler.sendMessage(msg);
                Log.d(TAG, "sendMessage: fail to send message due to json exception");
            }


        } else {
            if (type.equals(JSON_MSG)|type.equals(JSON_NEW_ROOM)){
                Message msg = new Message();
                msg.what = MSG_ERROR;
                msg.obj = mid;
                handler.sendMessage(msg);
            }

            Log.d(TAG, "sendMessage: fail to send message due to printwriter error");
        }
    }

    // 메세지 보낼 때
    public void sendMessage(Chat chat){
        if (out != null && !out.checkError()){
            try {

                JSONObject object = new JSONObject();
                JSONObject msgObject = new JSONObject();
                msgObject.put("unixTime",chat.getUnitTime());
                msgObject.put("rid",chat.getRid());
                msgObject.put("body",chat.getBody());
                msgObject.put("id",id); // 아래에 핸들러에게 보낼 때 경우 때문에 넣음.
                msgObject.put("mid",chat.getMid());
                object.put(JSON_MSG,msgObject);

                String message = object.toString();

                out.println(message);
                out.flush(); // TODO: 2017. 8. 19. flush ?

                Message msg = new Message();
                msg.obj = MyUtil.readJSONObject(message);
                msg.what = SEDNING;
                handler.sendMessage(msg);
                Log.d(TAG, "sendMessage: "+message);

            } catch (JSONException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = MSG_ERROR;
                msg.obj = chat.getMid();
                // TODO: 2017. 9. 8. mid같이보내서 sqlite 처리
                handler.sendMessage(msg);
                Log.d(TAG, "sendMessage: fail to send message due to json exception");
            }
        } else {
            Message msg = new Message();
            msg.what = MSG_ERROR;
            msg.obj = chat.getMid();
            // TODO: 2017. 9. 8. mid같이보내서 sqlite 처리
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
            } catch (RuntimeException e1){
                e1.printStackTrace();
                Log.e(TAG, "stopClient: no established connection" );
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

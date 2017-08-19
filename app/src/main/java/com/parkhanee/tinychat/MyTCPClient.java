package com.parkhanee.tinychat;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MyTCPClient {

    private final String TAG = "MyTCPClient";
    private final Handler handler;
    private BufferedReader in;
    private PrintWriter out;
    private boolean run = false;
    private MessageCallback listener = null;
    private String incomingMessage;
    private String server_ip, server_port, id, rid;

    /**
     * TCPClient class constructor, which is created in AsyncTasks after the button click.
     * @param handler Handler passed as an argument for updating the UI with sent messages
     * @param listener Callback interface object
     * @param strings server ip, server tcp port, user id, room id
     */
    public MyTCPClient(Handler handler, MessageCallback listener, String... strings){
        this.listener = listener;
        this.handler = handler;
        server_ip = strings[0];
        server_port = strings[1];
        id = strings[2];
        rid = strings[3];
    }

    public void run() {

        run = true;

        try {

            InetAddress serverAddr = InetAddress.getByName(server_ip);

            Log.d(TAG, "run: connecting . . .");

            handler.sendEmptyMessageDelayed(ChatActivity.CONNECTING,1000);

            Socket socket = new Socket(serverAddr, Integer.parseInt(server_port));

            try {
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d(TAG, "run: in/out created");
                handler.sendEmptyMessageDelayed(ChatActivity.CONNECTED,1000);

                // TODO: 2017. 8. 18. 이렇게 room_id와 id를 따로 보내면 보낸대로 도착하나 ??? 아니면 pool 만들어서 관리 해야 .
                this.sendMessage(rid);
                this.sendMessage(id);

                while (run){
                    incomingMessage= in.readLine();
                    if (incomingMessage!=null && listener !=null){
                        /**
                         * Incoming message is passed to MessageCallback object.
                         * Next it is retrieved by AsyncTask and passed to onPublishProgress method.
                         *
                         */
                        listener.callbackMessageReceiver(incomingMessage);
                        Log.d(TAG, "run: received message : "+incomingMessage);
                    }
                    incomingMessage = null;
                }


            } catch (IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessageDelayed(ChatActivity.ERROR,2000);
            } finally {
                out.flush();
                out.close();
                in.close();
                socket.close();
//                    handler.sendEmptyMessageDelayed(SENT,3000); // TODO: 2017. 8. 19.  sent 가 아니라 shutdown ?
                Log.d(TAG, "run: socket closed");
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            Log.d(TAG, "run: 서버와 연결 실패");
            handler.sendEmptyMessageDelayed(ChatActivity.ERROR,2000);
        }

    }

    // 서버에 메세지 전송
    public void sendMessage(String message){
        if (out != null && !out.checkError()){
            out.println(message);
            out.flush(); // TODO: 2017. 8. 19. flush ?
            Message msg = new Message();
            msg.obj = message;
            msg.what = ChatActivity.SENDING;
            handler.sendMessageDelayed(msg,1000);// TODO: 2017. 8. 19. SENT는 어디에 ?
            Log.d(TAG, "sendMessage: "+message);
        }
    }

    public void stopClient(){
        Log.d(TAG, "stopClient: ");
        run = false;
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

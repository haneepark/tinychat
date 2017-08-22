package com.parkhanee.tinychat;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 *
 * AsyncTask class which manages connection with server app and is sending shutdown command.
 */
public class MyTcpAsync extends AsyncTask<String, String, MyTCPClient> {
    private MyTCPClient tcpClient;
    private Handler handler;
    private static final String TAG = "MyTcpAsync";
    public static final String STOP = "STOP TCP CLIENT";

    public MyTcpAsync(Handler handler){
        this.handler = handler;
    }

    /**
     * @param strings server ip, server tcp port, user id, room id
     * */
    @Override
    protected MyTCPClient doInBackground(String... strings) {
        Log.d(TAG, "doInBackground: "+strings.length);


        if (strings.length <= 4) { // parameter 가 다 안들어온 경우 그냥 종료
            Log.d(TAG, "doInBackground: strings.length <= 4, thus exit MyTcpAsync");
            return null;
        }


        if (tcpClient == null) { // tcp 연결을 멈추기 위해서라고 하더라고 일단 객체는 무조건 받아와야 함.
            try {
                tcpClient = MyTCPClient.getInstance(
                        handler,
                        new MyTCPClient.MessageCallback() {

                            @Override
                            public void callbackMessageReceiver(String message) {
                                // tcpClient 에서 메세지를 받으면 여기로 넘어옴
                                publishProgress(message);
                            }
                        },
                        strings
                );
            } catch (NullPointerException e) {
                Log.d(TAG, "doInBackground: null pointer exception");
                e.printStackTrace();
            }

        }


        if (strings[4].equals(STOP)){ // tcp 연결 종료

            Log.d(TAG, "doInBackground: tcp client is null ? "+ (tcpClient ==null));
            // run == true 이면 멈추고, tcpClient객체 없앰.
            if (tcpClient.isRunning()){
                tcpClient.stopClient();
            }


        } else { // 메세지 보내는 경우

            if (!tcpClient.isRunning()) { // socket연결 안되어 있던 경우

                // tcpClient를 run시키는 async의 경우에는 sendMessage메서드가 아니라 run의 parameter로 메세지를 보낸다.
                // 왜냐하면 해당 async의 쓰레드가 tcpClient run 하는 곳에 가기 때문에 !! run이 다 종료가 된 후에야 async에 와서 이 줄 이후 코드(sendMessage메서드)가 실행되기 때문에, run이 종료되고 나서야 닫힌 socket에 메세지를 보내려고 시도하게 된다.
                tcpClient.run(strings[4]);
            } else {
                tcpClient.sendMessage(strings[4]);
            }

        } // 메세지 보내는 경우

        return null;
    }

    /**
     * Overriden method from AsyncTask class. Here we're checking if server answered properly.
     * @param values If "restart" message came, the client is stopped and computer should be restarted.
     *               Otherwise "wrong" message is sent and 'Error' message is shown in UI.
     */
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        // TODO: 2017. 8. 19. 메세지 도착
        Message msg = new Message();
        msg.obj = values[0];
        msg.what = ChatActivity.RECEIVED;
        handler.sendMessage(msg);

        Log.d(TAG, "onProgressUpdate: values : "+values[0]);
    }

}

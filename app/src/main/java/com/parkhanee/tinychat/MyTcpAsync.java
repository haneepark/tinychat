package com.parkhanee.tinychat;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Mariusz on 15.10.14.
 *
 * AsyncTask class which manages connection with server app and is sending shutdown command.
 */
public class MyTcpAsync extends AsyncTask<String, String, MyTCPClient> {
    private MyTCPClient tcpClient;
    private Handler handler;
    private static final String TAG = "MyTcpAsync";
//        private static final String COMAND = "shutdown -s";

    public MyTcpAsync(Handler handler){
        this.handler = handler;
    }

    /**
     * @param strings server ip, server tcp port, user id, room id
     * */
    @Override
    protected MyTCPClient doInBackground(String... strings) {
        Log.d(TAG, "doInBackground: ");
        try {
            tcpClient = new MyTCPClient(
                    handler,
                    new MyTCPClient.MessageCallback(){

                        @Override
                        public void callbackMessageReceiver(String message) {
                            // tcpClient 에서 메세지를 받으면 여기로 넘어옴
                            publishProgress(message);
                        }},
                    strings
            );
        } catch (NullPointerException e){
            Log.d(TAG, "doInBackground: null pointer exception");
            e.printStackTrace();
        }
        tcpClient.run();
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
        Log.d(TAG, "onProgressUpdate: values : "+values.toString());
        Message msg = new Message();
        msg.obj = values[0];
        msg.what = ChatActivity.RECEIVED;
        handler.sendMessageDelayed(msg,1000);
    }

    @Override
    protected void onPostExecute(MyTCPClient tcpClient) {
        super.onPostExecute(tcpClient);
        Log.d(TAG, "onPostExecute: ");
        if (tcpClient != null && tcpClient.isRunning()){
            tcpClient.stopClient();
            handler.sendEmptyMessageDelayed(ChatActivity.SHUTDOWN,2000);
        }
    }
}

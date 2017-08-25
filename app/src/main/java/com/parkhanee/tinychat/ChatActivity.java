package com.parkhanee.tinychat;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.parkhanee.tinychat.classbox.Room;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "ChatActivity";
    private TextView chatTextView;

    EditText et;
    Context context = this;
    Room room;

    MyPreferences pref;

    String id; // 내 아이디
    String friend_id; // 일대일 방의 친구 아이디
    String rid; // 채팅방 아이디


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (pref == null) {
            pref = MyPreferences.getInstance(context);
        }

        id = pref.getString("id");

        // 방 정보 설정하기 .
        rid = getIntent().getStringExtra("rid");
//        room = pref.getRoomFromId(rid);

        if (room==null){
            // TODO: 2017. 8. 22. it's an empty room !
        }

        chatTextView = (TextView) findViewById(R.id.textView);
        et = (EditText) findViewById(R.id.et_chat);
        (findViewById(R.id.btn_sendMsg)).setOnClickListener(this);

    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sendMsg : // 메세지 "전송" 버튼
                String msg = et.getText().toString();
                if (!msg.equals("")){ // TODO: 2017. 8. 22. 메세지 입력 안된 경우에는 전송안됨. 이때 전송 버튼 비활성화 할까 ?
                    //  send msg
                    new TcpAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,msg);
                    et.setText("");
//                handler.sendEmptyMessage(SENDING);
                }

                break;
        }
    }

    private class TcpAsyncTask extends AsyncTask<String, Void, Void> {
        private static final String TAG = "TcpAsyncTask";
        String msg;

        @Override
        protected Void doInBackground(String... strings) {
            msg = strings[0];
            MyTCPClient tcpClient = null;
            try {
                tcpClient = MyTCPClient.getInstance(null,"");
                tcpClient.sendMessage(MyTCPClient.JSON_MSG,rid,strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "doInBackground: tcpClient not initialized yet");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: "+msg);
        }
    }

}


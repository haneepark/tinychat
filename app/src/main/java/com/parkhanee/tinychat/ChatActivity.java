package com.parkhanee.tinychat;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
    MySQLite sqLite;

    String id; // 내 아이디
    String friend_id, friend_name; // 일대일 방의 친구 아이디, 이름
    String rid; // 채팅방 아이디


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar22);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // do not show default name text and instead, show the textView i included
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
        // TODO: 2017. 8. 26. add onClick for back button presssed


        if (pref == null) {
            pref = MyPreferences.getInstance(context);
        }
        if (sqLite==null){ sqLite = MySQLite.getInstance(ChatActivity.this); }
        id = pref.getString("id");

        chatTextView = (TextView) findViewById(R.id.textView);
        et = (EditText) findViewById(R.id.et_chat);
        (findViewById(R.id.btn_sendMsg)).setOnClickListener(this);

        // 방 정보 설정하기 .
        rid = getIntent().getStringExtra("rid");
        room = pref.getRoomFromId(rid);

        // set up toolbar title
        if (room==null){ // empty room (== not saved in pref)
            if(sqLite.getFriendFromRid(rid)!=null) { // 일대일 방인데 방이 만들어진건 아닌 경우.
                String name = sqLite.getFriendFromRid(rid).getName();
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText("empty room : "+name);
            } else {
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText("empty room ! ");
            }
        } else { // not an empty room
            if (room.isPrivate()){
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText(room.getParticipant().getName());
            } else {
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText("그룹채팅");
            }

            if (sqLite.getAllChatInARoom(rid)!=null){
                chatTextView.setText(sqLite.getAllChatInARoom(rid).toString());
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

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
            // TODO: 2017. 8. 26. adapter.notifyDateSetChanged
            if (sqLite.getAllChatInARoom(rid)!=null){
                chatTextView.setText(sqLite.getAllChatInARoom(rid).toString());
            }
        }
    }

}


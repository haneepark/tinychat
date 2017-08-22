package com.parkhanee.tinychat;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    private Handler handler;
    static final int CONNECTING = 1; // 서버와 연결중
    static final int CONNECTED = 0; // 서버와 연결중
    static final int READY_TO_TALK = 7; // 사용자 아이디, 방아이디 식별 완료
    static final int SENDING = 2; // 서버로 메세지 보내는 중
    static final int SENT = 3;
    static final int RECEIVED = 4;  // 서버로부터 메세지 받음
    static final int SHUTDOWN = 5;
    static final int ERROR = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (pref == null) {
            pref = MyPreferences.getInstance(context);
        }

        id = pref.getString("id");
        rid = getIntent().getStringExtra("rid");
        // TODO: 2017. 8. 18. pref에서 참여자 아이디 가져오기
        room = MyUtil.initRoom(context, pref, rid);

        chatTextView = (TextView) findViewById(R.id.textView);
        et = (EditText) findViewById(R.id.et_chat);
        (findViewById(R.id.btn_sendMsg)).setOnClickListener(this);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
//                    case CONNECTING :
//                        Toast.makeText(context, "connecting", Toast.LENGTH_SHORT).show();
//                        break;
//                    case CONNECTED :
//                        Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();
//                        break;
                    case READY_TO_TALK:
//                        Toast.makeText(context, "ready to talk", Toast.LENGTH_SHORT).show();
                        chatTextView.setText(chatTextView.getText().toString()+"\n ready to talk");
                        break;
                    case SENT :
                        Toast.makeText(context, "sent : "+msg.obj, Toast.LENGTH_SHORT).show();
//                        chatTextView.setText(chatTextView.getText().toString()+" \n 나 : "+msg.obj);
                        break;
                    case SENDING :
                        Toast.makeText(context, "sending", Toast.LENGTH_SHORT).show();
                        break;
                    case RECEIVED : // 메세지 도착
//                        Toast.makeText(context, "received : "+msg.obj, Toast.LENGTH_SHORT).show();
                        chatTextView.setText(chatTextView.getText().toString()+"\n"+msg.obj);
                        break;
                    case SHUTDOWN :
                        Toast.makeText(context, "shutdown", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR :
                        Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
//
//        // TODO: 2017. 8. 19. start Async and pass handler to it
//        MyTcpAsync asyncTask = new MyTcpAsync(handler);
//        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,getString(R.string.server_ip),getString(R.string.server_tcp_port),id,rid);

    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sendMsg : // 메세지 "전송" 버튼
                String msg = et.getText().toString();
                if (!msg.equals("")){ // TODO: 2017. 8. 22. 메세지 입력 안된 경우에는 전송안됨. 이때 전송 버튼 비활성화 할까 ?
                    //  send msg
                    executeAsyncTask(msg);
                    et.setText("");
//                handler.sendEmptyMessage(SENDING);
                }

                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        executeAsyncTask(MyTcpAsync.STOP);
    }

    public void executeAsyncTask(String msg){
        MyTcpAsync m = new MyTcpAsync(handler);
        m.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,getString(R.string.server_ip),getString(R.string.server_tcp_port),id,rid,msg);
        if (AsyncTask.Status.RUNNING != m.getStatus()){
            Toast.makeText(context, "async is NOT running", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "executeAsyncTask: async is NOT running : "+ msg);
        } else{
            Toast.makeText(context, "async is RUNNING", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "executeAsyncTask: async is RUNNING : " + msg);
        }
    }
}


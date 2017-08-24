package com.parkhanee.tinychat;

import android.content.Context;
import android.os.AsyncTask;
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

import static com.parkhanee.tinychat.MyTCPClient.ERROR;
import static com.parkhanee.tinychat.MyTCPClient.INFO;
import static com.parkhanee.tinychat.MyTCPClient.READY_TO_TALK;
import static com.parkhanee.tinychat.MyTCPClient.RECEIVED;
import static com.parkhanee.tinychat.MyTCPClient.SENDING;
import static com.parkhanee.tinychat.MyTCPClient.SENT;
import static com.parkhanee.tinychat.MyTCPClient.SHUTDOWN;


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
        // TODO: 2017. 8. 18. pref에서 참여자 아이디 가져오기
//        room = MyUtil.initRoom(context, pref, rid);
        if (room==null){
            // TODO: 2017. 8. 22. it's an empty room !
        }

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
                        chatTextView.setText(chatTextView.getText().toString()+" \n"+msg.obj);
                        break;
                    case SENDING :
                        Toast.makeText(context, "sending", Toast.LENGTH_SHORT).show();
                        break;
                    case RECEIVED : // 친구에게서 채팅 메세지 도착
//                        Toast.makeText(context, "received : "+msg.obj, Toast.LENGTH_SHORT).show();
                        chatTextView.setText(chatTextView.getText().toString()+"\n"+msg.obj);
                        break;
                    case INFO : // 서버의 알림 메세지 도착
                        chatTextView.setText(chatTextView.getText().toString()+"\n 알림 : "+msg.obj);
                        break;
                    case SHUTDOWN :
                        Toast.makeText(context, "shutdown", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR :
                        Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                        break;
                    case  100 : // async exit 100
                        Toast.makeText(context, "async exit : "+msg.obj, Toast.LENGTH_SHORT).show();
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
//                    executeAsyncTask(msg);
                    et.setText("");
//                handler.sendEmptyMessage(SENDING);
                }

                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        executeAsyncTask(MyTcpAsync.STOP);
    }

//    public void executeAsyncTask(String msg){
//        MyTcpAsync m = new MyTcpAsync(handler);
//        m.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,getString(R.string.server_ip),getString(R.string.server_tcp_port),id,rid,msg);
//        if (AsyncTask.Status.RUNNING != m.getStatus()){
//            Toast.makeText(context, "async is NOT running : "+ msg, Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "executeAsyncTask: async is NOT running : "+ msg);
//        } else{
//            Toast.makeText(context, "alive async : "+ msg, Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "executeAsyncTask: alive async : " + msg);
//        }
//    }
}


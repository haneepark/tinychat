package com.parkhanee.tinychat;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Room;



public class ChatActivity extends AppCompatActivity {
    private final String TAG = "ChatActivity";
    private TextView chatTextView;

    EditText et;
    Context context = this;
    Room room;

    MyPreferences pref;

    String id; // 내 아이디
    String friend_id; // 일대일 방의 친구 아이디
    String rid; // 채팅방 아이디

    private MyTcpAsync asyncTask;
     static final int CONNECTING = 1; // 서버와 연결중
    static final int CONNECTED = 0; // 서버와 연결중

    static final int SENDING = 2; // 서버로 메세지 보내는 중
    static final int SENT = 3;
//     static final int RECEIVING = 4;
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


        // TODO: 2017. 8. 19. start Async and pass handler to it
        asyncTask = new MyTcpAsync(new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case CONNECTING :
                        Toast.makeText(context, "connecting", Toast.LENGTH_SHORT).show();
                        break;
                    case CONNECTED :
                        Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();
                        break;
                    case SENDING :
                        Toast.makeText(context, "sending : "+msg.obj, Toast.LENGTH_SHORT).show();
                        break;
                    case SENT :
                        Toast.makeText(context, "sent", Toast.LENGTH_SHORT).show();
                        break;
                    case RECEIVED :
                        Toast.makeText(context, "received : "+msg.obj, Toast.LENGTH_SHORT).show();
                        break;
                    case SHUTDOWN :
                        Toast.makeText(context, "shutdown", Toast.LENGTH_SHORT).show();
                        break;
                    case ERROR :
                        Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        asyncTask.execute(getString(R.string.server_ip),getString(R.string.server_tcp_port),id,rid);

    }

    public void onClick(View view) {
        try {
            String msg = et.getText().toString();
            // TODO: 2017. 8. 19. send msg
            et.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}


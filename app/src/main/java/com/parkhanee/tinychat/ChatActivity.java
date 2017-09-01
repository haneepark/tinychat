package com.parkhanee.tinychat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Chat;
import com.parkhanee.tinychat.classbox.Friend;
import com.parkhanee.tinychat.classbox.Room;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener, MyRecyclerView.OnKeyboardStatusChangeListener, MyTCPService.OnNewMessageReceivedListener {

    private final String TAG = "ChatActivity";

    EditText et;
    Context context = this;
    Room room;

    MyPreferences pref;
    MySQLite sqLite;

    String id; // 내 아이디
    Friend friend; // 일대일 방의  --> 친구 이름, 프로필 사진 가지고 메세지 보이기
    String rid; // 채팅방 아이디

    private MyRecyclerView mRecyclerView;
    private ChatAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    Toolbar toolbar;

    boolean serviceBound=false;
    MyTCPService tcpService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = (Toolbar) findViewById(R.id.toolbar22);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // do not show default name text and instead, show the textView i included
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
        // TODO: 2017. 8. 26. add onClick for back button presssed


        if (pref == null) {
            pref = MyPreferences.getInstance(context);
        }
        if (sqLite==null){ sqLite = MySQLite.getInstance(ChatActivity.this); }
        id = pref.getId();

        et = (EditText) findViewById(R.id.et_chat);
        (findViewById(R.id.btn_sendMsg)).setOnClickListener(this);

        // recycler view
        mRecyclerView = (MyRecyclerView) findViewById(R.id.chat_recycler);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnKeyboardStatusChangeListener(this);
        mAdapter = new ChatAdapter(ChatActivity.this,id);
        mRecyclerView.setAdapter(mAdapter);

        // 방 정보 설정하기 .
        rid = getIntent().getStringExtra("rid");
        room = pref.getRoomFromId(rid);

        Log.d(TAG, "onCreate: rid: "+rid);
        if (room!=null){
            Log.d(TAG, "onCreate: room: "+room.toString());
        } else {
            Log.d(TAG, "onCreate: room: "+null);
        }


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
                friend = room.getParticipant();
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText(friend.getName());
            } else {
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText("그룹채팅");
            }

            if (sqLite.getAllChatInARoom(rid)!=null){
                // specify an adapter (see also next example)
                mAdapter.setChatArrayList(sqLite.getAllChatInARoom(rid));
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (rid==null){
            Toast.makeText(context, "onResume : rid is null ?? ", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onResume: rid is null?");
        }
        Intent intent = new Intent(ChatActivity.this,MyTCPService.class);
//        intent.putExtra("chatActivityBound",true);
//        intent.putExtra("rid",rid);
        bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
        serviceBound = true;
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyTCPService.MyBinder myBinder = (MyTCPService.MyBinder) iBinder;
            tcpService = myBinder.getService();
            tcpService.setOnNewMessageRecievedListener(ChatActivity.this,rid);
            // tcpClient = myBinder.getClient();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            tcpService.unsetOnNewMessageRecievedListener(ChatActivity.this);
            serviceBound = false;
        }
    };

    /**
     * 메세지 전송 후 tcpService에서 로컬디비에 새 방 추가, 메세지 추가 함.
     * 그 후에 여기서! 로컬디비에서 새로 저장된 내용을 출력해서 액티비티에 그려줌.
     * */
    @Override
    public void onMessageReceivedCallback() {

        Log.d(TAG, "onMessageReceivedCallback: ");

        // 빈 방에서 보통 방이 되는 경우 toolbar title 설정
        if (room==null){
            Log.d(TAG, "onMessageReceivedCallback: room is null");
            room = pref.getRoomFromId(rid);
            if (room!=null) { // pref에서 새로 방이 생긴경우!
                Log.d(TAG, "onMessageReceivedCallback: room is not null now ! ");
                if (room.isPrivate()){
                    Log.d(TAG, "onMessageReceivedCallback: room is private");
                    friend = room.getParticipant();
                    ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText(friend.getName());
                } else {
                    ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText("그룹채팅");
                }
            }
        }

        // db에 방금 보낸 채팅을 넣는건 TCPService에서 했으므로, 여기서는 뷰 띄워주기만 하면 됨.
        if (sqLite.getAllChatInARoom(rid)!=null){ // 여기서는 방금 하나 보냈으니까  null이 아닌게 정상
            // specify an adapter (see also next example)
            mAdapter.setChatArrayList(sqLite.getAllChatInARoom(rid));
            mAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        } else {
            Log.d(TAG, "onMessageReceivedCallback: chat is null?");
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound){
            Log.d(TAG, "onStop: unbind service: rid: "+rid);
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_sendMsg : // 메세지 "전송" 버튼
                String msg = et.getText().toString();
                if (!msg.equals("")){ // TODO: 2017. 8. 22. 메세지 입력 안된 경우에는 전송안됨. 이때 전송 버튼 비활성화 할까 ?
                   if ( MyUtil.IsNetworkConnected(ChatActivity.this)){
                       //  send msg
                       new TcpAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,msg);
                       et.setText("");
                   } else {
                       // TODO: 2017. 9. 1. 경고
                       Toast.makeText(context, "인터넷에 연결되어있지 않음", Toast.LENGTH_SHORT).show();
                   }

                }

                break;
        }
    }

    @Override
    public void onKeyboardStatusChangeCallback(boolean shown) { // keyboard hide/shown 상태 바뀔 때 마다 실행
        if (shown){
            // 키보드가 보이면 레이아웃 위로 밀어줌
            if (mAdapter.getItemCount()>2){ // 기존 채팅이 두개 이상 존재 하는 경우
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }

    private class TcpAsyncTask extends AsyncTask<String, Void, Void> {
        private static final String TAG = "TcpAsyncTask";
        String msg;
        boolean isSent=true;

        @Override
        protected Void doInBackground(String... strings) {
            msg = strings[0];
            MyTCPClient tcpClient = null;

                tcpClient = MyTCPClient.getInstance();

            if (tcpClient!=null){
                if (tcpClient.isRunning()){
                    tcpClient.sendMessage(MyTCPClient.JSON_MSG,rid,strings[0]);
                    isSent=true;
                } else {
                    // tcpClient 인스턴스가 생성은 되었지만 아직 서버와 연결은 안된 상태이므로 기다렸다가 onPostExecute로 가서 새 async 실행.
                    // 그런데 어떻게든 작업이 꼬여서 기다리는 async가 여러개 계속 생성되면 과부하 문제 생길 수 있겠다.
                    try {
                        Log.d(TAG, "doInBackground: tcpClient is not running, thus wait");
                        Thread.sleep(1000);
                        isSent=false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else { // 서비스가 이미 시작되지 않은 경우 또는 서비스가 tcpClient 시작시키지 않은 경우

                Log.d(TAG, "doInBackground: tcpClient not initialized yet, thus start new Service");

                // 서비스 시작
                Intent i = new Intent(ChatActivity.this,MyTCPService.class);
                startService(i);
                isSent=false;
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: "+msg);
            if (!isSent&&MyUtil.IsNetworkConnected(ChatActivity.this)){
                Log.d(TAG, "onPostExecute: run new asyncTask");
                new TcpAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,msg);
            }
        }
    }

}


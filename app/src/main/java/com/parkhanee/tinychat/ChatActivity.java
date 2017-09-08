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

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener, MyRecyclerView.OnKeyboardStatusChangeListener, MyTCPService.OnNewMessageReceivedListener {

    private final String TAG = "ChatActivity";

    EditText et;
    Context context = this;
    Room room; // is null when it's an empty room

    MyPreferences pref;
    MySQLite sqLite;

    String id; // 내 아이디
    Friend friend; // 일대일 방의  --> 친구 이름, 프로필 사진 가지고 메세지 보이기
    String rid=""; // 채팅방 아이디

    // Chat Recycler View
    private MyRecyclerView mRecyclerView;
    private ChatAdapter chatAdapter;
    Toolbar toolbar;

    // Sending Chat Recycler View
    private MyRecyclerView mRecyclerView2;
    private ChatSendingAdapter chatSendingAdapter;


    boolean serviceBound=false;
    MyTCPService tcpService;

    boolean isPrivate;
    String ppl=null; // null 이 아니면 새로운 단체방 만들기 중!
                     // AddRoomActivity 에서 ChatActivity로 넘어온 다음에 처음 채팅 보내서 다시 받기 전인 상태 !!

    Intent newIntent=null;

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

        et = (EditText) findViewById(R.id.et_chat);
        (findViewById(R.id.btn_sendMsg)).setOnClickListener(this);

        // recycler view
        mRecyclerView = (MyRecyclerView) findViewById(R.id.chat_recycler);
        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnKeyboardStatusChangeListener(this);

        mRecyclerView2 = (MyRecyclerView) findViewById(R.id.chat_recycler2);
        LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(this);
        mLayoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView2.setLayoutManager(mLayoutManager2);
        chatSendingAdapter = new ChatSendingAdapter();
        mRecyclerView2.setAdapter(chatSendingAdapter);
        mRecyclerView2.setOnKeyboardStatusChangeListener(this);
    } // onCreate 에는 여러 채팅방이 이 액티비티로 실행될 때 공통으로 써도 상관없는 요소들을 초기화시킴.

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        newIntent=intent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (newIntent==null){
            newIntent = getIntent();
        }

        Toast.makeText(context, "onResume"+newIntent.getStringExtra("rid"), Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onResume: "+newIntent.getStringExtra("rid"));

        id = pref.getId();
        chatAdapter = new ChatAdapter(ChatActivity.this,id);
        mRecyclerView.setAdapter(chatAdapter);

        // TODO: 2017. 9. 8. pref.getAllSendingChat()
        // mRecyclerView2.sendingChat()

        isPrivate = newIntent.getBooleanExtra("isPrivate",true);

        /*
         * 방 정보 설정하기.
         *
         * ChatActivity 에서부터 온 경우
         *      1. 빈방 아님 && (개인방|단체방)
         *
         * AddRoomActivity 에서부터 온 경우
         *      2. 단체방 && 빈방
         *      3. 개인방 && (빈방|빈방아님)
         *
         * */

        if (newIntent.hasExtra("rid")){ // 빈방 아님 && (개인방|단체방)

            rid = newIntent.getStringExtra("rid");
            room = pref.getRoomFromId(rid);
            if (room!=null) friend = room.getParticipant();
            Log.d(TAG, "onResume: 1");
        } else if (!isPrivate && newIntent.hasExtra("ppl")){  // 단체방 && 빈방
            // 여기는 아직 rid 는 "" ,  room 은 null 임 !!
            // 참여자 정보 가져오기 : 단체방이고 empty room 인 경우 에만 참여자정보를 pref 가 아니라 인텐트에서 가져온다
            // ppl  -  2:68620823,11111111
            this.ppl = newIntent.getStringExtra("ppl");
            Log.d(TAG, "onResume: ppl: "+ppl);

        } else if (newIntent.hasExtra("id")){ // 개인방 && (빈방|빈방아님)
            String friend_id = newIntent.getStringExtra("id");
            friend = sqLite.getFriend(friend_id);
            rid = friend.getRid();
            room = pref.getRoomFromId(rid);
            isPrivate=false;
        }

        Log.d(TAG, "onResume: rid: "+rid);
        if (room!=null){
            Log.d(TAG, "onResume: room: "+room.toString());
        } else {
            Log.d(TAG, "onResume: room: "+null);
        }


        // set up toolbar title
        if (room==null | rid.equals("")){ // empty room (== not saved in pref)
            if (rid.equals("")){ // 단체방
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText("empty room ! ");
            } else if(sqLite.getFriendFromRid(rid)!=null) { // 일대일 방인데 방이 만들어진건 아닌 경우. // FIXME: 2017. 9. 1. 단체방일때 실행안함
                String name = sqLite.getFriendFromRid(rid).getName();
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText("empty room : "+name);
            } else {
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText(" ?? ");
            }
        } else { // not an empty room
            Log.d(TAG, "onResume: 2");
            if (room.isPrivate()){
                Log.d(TAG, "onResume: 3");
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText(friend.getName());
            } else {
                Log.d(TAG, "onResume: 4");
                ((TextView)toolbar.findViewById(R.id.my_tool_bar_title)).setText("그룹채팅");
            }

            if (sqLite.getAllChatInARoom(rid)!=null){
                Log.d(TAG, "onResume: 5");
                // specify an adapter (see also next example)
                chatAdapter.setChatArrayList(sqLite.getAllChatInARoom(rid));
                mRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                chatAdapter.notifyDataSetChanged();
            }
        }

        if (rid==null){
            Toast.makeText(context, "onResume : rid is null ?? ", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onResume: rid is null?");
        }

        Intent intent = new Intent(ChatActivity.this,MyTCPService.class);
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
            Toast.makeText(context, "unbind", Toast.LENGTH_SHORT).show();
//            tcpService.unsetOnNewMessageRecievedListener(ChatActivity.this);
            serviceBound = false;
        }

    };

    /**
     * 메세지 전송 후 tcpService에서 로컬디비에 새 방 추가, 메세지 추가 함.
     * 그 후에 여기서! 로컬디비에서 새로 저장된 내용을 출력해서 액티비티에 그려줌.
     * */
    @Override
    public void onMessageReceivedCallback(String rid,String mid) {
        Log.d(TAG, "onMessageReceivedCallback: ");

        if (rid.equals("")){  // 단체 방 처음 만들 때
            ppl=null;
            this.rid = rid;
            // rid제대로 설정
            unbindService(serviceConnection);
            Intent intent = new Intent(ChatActivity.this,MyTCPService.class);
            bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
            serviceBound = true;
        }

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

        // sending message Recycler View 에서 보내는 중 메세지 없애기
        chatSendingAdapter.deleteSendingChat(mid);

        // db에 방금 보낸 채팅을 넣는건 TCPService에서 했으므로, 여기서는 뷰 띄워주기만 하면 됨.
        if (sqLite.getAllChatInARoom(rid)!=null){ // 여기서는 방금 하나 보냈으니까  null이 아닌게 정상 // FIXME: 2017. 9. 2. rid equals ""
            // specify an adapter (see also next example)
            chatAdapter.setChatArrayList(sqLite.getAllChatInARoom(rid));
            chatAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        } else {
            Log.d(TAG, "onMessageReceivedCallback: chat is null?");
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        if (serviceBound){
            Log.d(TAG, "onStop: unbind service: rid: "+rid);
            tcpService.unsetOnNewMessageRecievedListener(ChatActivity.this,rid);
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
                       new TcpAsyncTask(msg).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                       et.setText("");
                   } else {
                       // TODO: 2017. 9. 1. 경고
                       Toast.makeText(context, "인터넷에 연결되어있지 않음", Toast.LENGTH_SHORT).show();
                   }

                }

                break;
        }
    }

    // 두개의 리사이클러뷰의 리스너 모두 여기로 옴
    @Override
    public void onKeyboardStatusChangeCallback(boolean shown) { // keyboard hide/shown 상태 바뀔 때 마다 실행
        if (shown){
            // 키보드가 보이면 레이아웃 위로 밀어줌
            if (chatAdapter.getItemCount()>2){ // 기존 채팅이 두개 이상 존재 하는 경우
                mRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        }
    }

    private class TcpAsyncTask extends AsyncTask<String, Void, Void> {
        private static final String TAG = "TcpAsyncTask";
        String msg="empty message";
        boolean isSent=true; // false 이면 onPostExecute 에서 new async 실행
        Chat chat;

        public TcpAsyncTask(String msg) {
            super();
            this.msg = msg;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Date date = new Date();
            long time = date.getTime()/1000;
            Log.d(TAG, "sendMessage: unitTime " +time );
            String unixTime = String.valueOf(time);

            int randomNum = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                randomNum = ThreadLocalRandom.
                        current().nextInt(0, 1000 + 1); // generage random within 10 to 100
            }
            String mid = String.valueOf(randomNum)+rid+unixTime;

            chat = new Chat(mid,rid,id,msg,unixTime);

            chatSendingAdapter.sendingChat(chat);
            Log.d(TAG, "onPreExecute: "+chat.toString());
        }

        @Override
        protected Void doInBackground(String... strings) {
            MyTCPClient tcpClient = null;

                tcpClient = MyTCPClient.getInstance();

            if (tcpClient!=null){
                if (tcpClient.isRunning()){
                    if (!isPrivate&&ppl!=null){
                        // 단체방 새로 생성 request를 서버에 보냄
                        // json type , 단체방 사람수와 아이디 목록, 처음 보내고자하는 메세지
                        tcpClient.sendMessage(MyTCPClient.JSON_REQUEST,ppl,strings[0]);
                        isSent=true; // 메세지 내용 까지 같이 보내니까 true.
                        //ppl=null; // FIXME: 2017. 9. 2.  방 제대로 만들어진거 확인하고 ppl=null 설정 해야 함
                    }else { // 메세지 전송
                        tcpClient.sendMessage(chat);
                        isSent=true;
                    }

                } else {
                    // tcpClient 인스턴스가 생성은 되었지만 아직 서버와 연결은 안된 상태이므로 기다렸다가 onPostExecute로 가서 새 async 실행.
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
            // FIXME: 2017. 9. 2.  async가 끊임없이 다시 시작되는 문제 ..
//            if (!isSent&&MyUtil.IsNetworkConnected(ChatActivity.this)){
//                Log.d(TAG, "onPostExecute: run new asyncTask");
//                new TcpAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,msg);
//            }
        }
    }

}


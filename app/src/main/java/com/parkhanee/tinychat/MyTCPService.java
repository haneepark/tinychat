package com.parkhanee.tinychat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Chat;
import com.parkhanee.tinychat.classbox.Room;

import java.util.ArrayList;
import java.util.List;

import static com.parkhanee.tinychat.MyTCPClient.CONNECTED;
import static com.parkhanee.tinychat.MyTCPClient.CONNECTING;
import static com.parkhanee.tinychat.MyTCPClient.ERROR;
import static com.parkhanee.tinychat.MyTCPClient.INFO;
import static com.parkhanee.tinychat.MyTCPClient.NEW_ROOM;
import static com.parkhanee.tinychat.MyTCPClient.READY_TO_TALK;
import static com.parkhanee.tinychat.MyTCPClient.RECEIVED;
import static com.parkhanee.tinychat.MyTCPClient.SEDNING;
import static com.parkhanee.tinychat.MyTCPClient.SENT;
import static com.parkhanee.tinychat.MyTCPClient.SHUTDOWN;

/**
 * Created by parkhanee on 2017. 8. 23..
 *
 * tcpClient : 서버와 소켓연결 생성, 유지, 데이터 주고받기
 * tcpService(쓰레드1) : tcpClient 객체를 만들고 받은 메세지에 대해서 처리
 * tcpAsync(전송 할 때마다 새로운 쓰레드) : chatActivity 내부에서 존재하면서 "전송" 버튼을 누를 때 마다 새로 생성되어 메세지 전송 역할.
 *          메세지 전송 및 메인 액티비티와 interact
 *
 *
 * 1)
 * 처음 어플 켜서 메인액티비티 들어가면 소켓 연결 생성
 * --> 서버에 사용자아이디를 보내고 서버와 채팅연결 생성
 *  ( --> 메세지박스에 와있던 밀린 메세지 모두 받기 )
 *
 * 2)
 * 새로 채팅 메세지가 오면
 * --> 노티로 띄우기 + pref에 최근메세지 등록 + pref의 방목록에 없으면 새로운방 등록
 *
 * 3)
 * 채팅 액티비티 들어가서 전송버튼 누르면
 * --> if 바인드 안되어 있으면, 서비스와 채팅 액티비티를 바인드 (서비스가 현재 활성중인 rid 기억하도록)
 * --> 액티비티에 전송중인 메세지 그려주기
 * --> async를 새로 만들고 실행하여 전송하고자 하는 채팅 메세지를 전달
 * --> async가 tcpClient의 객체 받아오고, 해당 메세지를 tcpClient 통해 서버에게 전송
 * --> tcpClient가 서비스 에게, 서비스 가 액티비티 에게 전송 잘 되었다는 걸 알림
 * --> 1:1방이면 sqlite에서 아이디 받아와서 pref에 새로운 방 생성.
 * --> 액티비티에서 해당 메세지 전송완료 표시
 *
 * 4)
 * 현재 활성화된 채팅 액티비티에 메세지가 온 경우
 * --> if 바인드 안되어있으면, 서비스와 채팅 액티비티를 바인드
 * --> 서비스에서 채팅액티비티로 받은 메세지 전달해줌
 * --> 액티비티에서 그려줌
 *
 * 5)
 * 채팅액티비티 onStop
 * --> if 바인드 되어있으면, 바인드 풀기. (바인드만 해제되고 서비스는 계속 유지.)
 *
 * 6)
 * 네트워크 상태 변경이 감지되면
 * --> 네트워크 상태에 따라 서비스 생명 관리.
 *
 */

public class MyTCPService extends IntentService {
    private static final String TAG = "MyTCPService";
    private Handler handler=null;
    private MyPreferences pref=null;
    private MySQLite sqLite=null;
    private static MyTCPClient tcpClient=null;
    String id ;

    private IBinder binder = new MyBinder();

    String activeRoomId="";
    private final ArrayList<OnNewMessageReceivedListener> listeners = new ArrayList<>();
    public static final String roomTabOnBindRID="roomTabOnBindRID";

    public MyTCPService(String name) {
        super(name);
    }

    public MyTCPService() {
        super("MyTCPService");
    }

    public class MyBinder extends Binder {
        MyTCPService getService(){
            return MyTCPService.this;
        }

        MyTCPClient getClient(){
            return MyTCPService.tcpClient;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind: ");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");

        return false;        // return super.onUnbind(intent);
        // 한 서비스에 대해 bind가 여러번 생기면,
        // true : 첫번째는 onBind-onUnBind 두번째부터는 onRebind-onUnBind 호출
        // false : 첫번째는 onBind-onUnBind 호출, 그 뒤로는 아예 호출되지 않음
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "stopService: ");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tcpClient.isRunning()){
            tcpClient.stopClient();
        }
        Log.d(TAG, "onDestroy: ");
//        Toast.makeText(this, "서비스 onDestroy", Toast.LENGTH_SHORT).show();
    }



    /**
     * tcpClient로 서버와 소켓 연결 시작
     * */
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
//        Toast.makeText(this, "서비스 onStartCommand", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onStartCommand: ");

        if (pref==null){
            Log.d(TAG, "onStartCommand: init pref");
            pref = MyPreferences.getInstance(this);
        }

        id = pref.getId();
        if (id.equals("")){
            Log.e(TAG, "onStartCommand: id is empty !!! ");
        }

        if (sqLite==null){
            Log.d(TAG, "onStartCommand: init sqlite");
            sqLite = MySQLite.getInstance(MyTCPService.this);
        }



        if (handler==null){
            Log.d(TAG, "onStartCommand: init handler");
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch (msg.what){
                        case RECEIVED :  //  친구에게서 채팅메세지 도착
                            Toast.makeText(MyTCPService.this, "RECEIVED", Toast.LENGTH_SHORT).show();

                            @SuppressWarnings("unchecked")
                            List<String> result = (List<String>) msg.obj;


                            if (!result.get(2).equals(id)) { // 아이디가 내아이디가 아님 == 친구에게서 받은 메세지

                                Log.d(TAG, "handleMessage: received");

                                String rid = result.get(1);
                                String from = result.get(2); // 메세지 보낸 사람 아이디
                                String body = result.get(3);
                                String unixTime = result.get(4);
                                String mid = result.get(5);

                                // TODO: 2017. 8. 31.  메세지 보낸사람이 친구로 등록 안 되어있을 경우 ?
                                // TCPClient로 사람 정보 받아오기 요청 보내서 from의 정보 받아오기.
                                // sqLite에 친구임/아님 boolean값 추가 해서, 친구목록 뽑을때는 false인 항목 무시하고 뽑고. 아이디로 이름이나 방번호 찾을 때는 db에서 쿼리가능하도록.

                                /* 모르는 사람에게 메세지 오면(== from의 아이디를 가진 사람이 db에 없을 때)
                                * 1 일단 RECEIVED로 여기서 받고
                                * 2 메세지 정보 이외에 사람 정보도 같이 왔는지 확인하고 왔으면 db에 저장하고 아래 코드 실행,
                                * 3 안왔으면 아래 코드 실행하지 않고 tcpClient 통해서 사람정보 받아오기 요청 보냄.
                                * 4 1번부터 다시 실행
                                * */

                                // pref의 방목록에 없으면 새로운방 등록
                                if (!pref.isRoomSet(rid)){
                                    // 일대일 방 만!  단체방 등록은 여기가 아니라 NEW_ROOM 에서.
                                    Room room = new Room(rid,1,from,MyTCPService.this);
                                    pref.addRoom(room);
                                }

                                // SQLite에 메세지 등록
                                if (!sqLite.addChat(new Chat(mid,rid,from,body,unixTime))){ // addChat실패하면
                                    Toast.makeText(MyTCPService.this, "addChat failed", Toast.LENGTH_SHORT).show();
                                }


                                // 방금 도착한 메세지의 rid와 activeRoomId를 비교해서 같으면 노티 대신에 ChatActivity에 알린다 ! -->  UI update
                                // 또는 RoomTab에 bind되어 있는 걸 확인하고 그쪽으로 알림.                                  -->  UI update

                                if (rid.equals(activeRoomId)){
                                    for (OnNewMessageReceivedListener listener : listeners){
                                        listener.onMessageReceivedCallback(rid);
                                    }
                                } else {

                                    if (activeRoomId.equals(roomTabOnBindRID)){
                                        for (OnNewMessageReceivedListener listener : listeners){
                                            listener.onMessageReceivedCallback(rid);
                                        }
                                    }

                                    String date = MyUtil.UnixTimeToCustomDate(unixTime);
                                    String from_name = sqLite.getFriendName(from); // 보낸 사람 이름

                                    // 노티 띄우기
                                    Intent intent = new Intent(MyTCPService.this, ChatActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.putExtra("rid",rid);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(MyTCPService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

                                    sendMyNotification(pendingIntent,from_name,body+" "+rid,date);
                                }
                            }
                            else {
                                // Log.e(TAG, "handleMessage: 내가 보낸 메세지인데 RECEIVED로 옴");
                                // 무시
                                // TODO: 2017. 9. 2. 내가 보낸 메세지. 단체방에서 방 처음만들때 보내는 메세지 빼고는 전부 여기로 옴.
                                Toast.makeText(MyTCPService.this, "rid"+result.get(1)+" body"+result.get(3), Toast.LENGTH_SHORT).show();

                            }

                            break;
                        case NEW_ROOM :
                            Toast.makeText(MyTCPService.this, "NEW ROOM", Toast.LENGTH_SHORT).show();
                            @SuppressWarnings("unchecked")
                            List<String> result2 = (List<String>) msg.obj;
                            String rid2 = result2.get(1);
                            String from2 = result2.get(2); // 메세지 보낸 사람 아이디
                            String body2 = result2.get(3);
                            String unixTime2 = result2.get(4);
                            String mid2 = result2.get(5);
                            String ppl2 = result2.get(6);
                            String[] strings = ppl2.split(",");
                            int i=0;
                            ppl2="";
                            for (String s : strings){ // 내 아이디 빼고 다시 참여자 목록 만듦
                                if (!s.equals(id)){
                                    if (i>0){ppl2+=",";}
                                    ppl2 += s;
                                    i++;
                                }
                            }

                            // 새로운 다중채팅방을 로컬디비에 넣기
                            if (!pref.isRoomSet(rid2)){
                                pref.addRoom(new Room(rid2,strings.length-1,ppl2,MyTCPService.this));
                            }

                            // 새로운 메세지 로컬디비에 넣기
                            if (!sqLite.addChat(new Chat(mid2,rid2,from2,body2,unixTime2))){ // addChat실패하면
                                Toast.makeText(MyTCPService.this, "addChat failed", Toast.LENGTH_SHORT).show();
                            }


                            if (from2.equals(id)){ // 내가 보냈던 메세지라면 전송 완료 처리, 노티 x
                                // roomTab 또는 activeChatActivity 로 알림 보냄.
                                for (OnNewMessageReceivedListener listener : listeners){
                                    listener.onMessageReceivedCallback(rid2);
                                }
                            } else { // 다른사람에게 온 메세지라면 노티 처리

                                String date = MyUtil.UnixTimeToCustomDate(unixTime2);
                                String from_name = sqLite.getFriendName(from2); // 보낸 사람 이름

                                // 노티 띄우기
                                Intent intent = new Intent(MyTCPService.this, ChatActivity.class);
                                intent.putExtra("rid",rid2);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                PendingIntent pendingIntent = PendingIntent.getActivity(MyTCPService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

                                sendMyNotification(pendingIntent,from_name,body2,date);
                            }

                            // roomTab이 바인드되어있다면 리스너 통해서 알리기
                            if (activeRoomId.equals(roomTabOnBindRID)){
                                for (OnNewMessageReceivedListener listener : listeners){
                                    listener.onMessageReceivedCallback(rid2);
                                }
                            }

                            break;
                        case CONNECTING :
//                            Toast.makeText(MyTCPService.this, "connecting", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: connecting");
                            break;
                        case CONNECTED :
//                            Toast.makeText(MyTCPService.this, "connected", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: connected");
                            break;
                        case READY_TO_TALK:
                        Toast.makeText(MyTCPService.this, "ready to talk", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: ready to talk");
                            break;
                        case SENT : // 메세지 전송 성공.  내가 보냈던 메세지를 서버로부터 돌려받음.
                            Toast.makeText(MyTCPService.this, "SENT", Toast.LENGTH_SHORT).show();

                            // TODO: 2017. 8. 25. 채팅 액티비티에서 특정 메세지가 전송 완료 되었다는 거 알려주기.
                            // 채팅액티비티가 메세지를 보내기 시작 할 때 mid 만들어서
                            // async --> tcpClient --> 여기 차례로 mid를 전달하고
                            // 마지막으로 여기서 액티비티에게 mid 알려주면서 얘가 다 전송완료 됐어 라고 알려주어야 겠다..

                            @SuppressWarnings("unchecked")
                            List<String> result1 = (List<String>) msg.obj;

                            String rid = result1.get(1);
                            String from = result1.get(2); // 메세지 보낸 사람 아이디, 여기서는 내 아이디 여야 함.
                            String body = result1.get(3);
                            String unixTime = result1.get(4);
                            String mid = result1.get(5);

                            if (from.equals(id)){ // 보낸사람이 나인거 확인
                                // pref.rooms 에  rid 존재하지 않으면 방 새로 만들기
                                if (!pref.isRoomSet(rid)&&!rid.equals("")){
                                    //  from 에 넣을 상대방 아이디를 db에서 찾아서  friend객체 넣어줌
                                    Room room = new Room(rid,sqLite.getFriendFromRid(rid),MyTCPService.this);
                                    pref.addRoom(room);
                                }


                                // SQLite에 메세지 등록
                                if (!sqLite.addChat(new Chat(mid,rid,from,body,unixTime))){ // addChat 실패하면
                                    Toast.makeText(MyTCPService.this, "addChat failed", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "handleMessage: SENT: addChat failed" );
                                } else {
//                                    Toast.makeText(MyTCPService.this, "addChat OK", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "handleMessage: SENT: addChat OK");
                                }

                            }

                            if (rid.equals(activeRoomId) | activeRoomId.equals(roomTabOnBindRID)){
                                for (OnNewMessageReceivedListener listener : listeners){
                                    listener.onMessageReceivedCallback(rid);
                                }
                            }

//                            Toast.makeText(MyTCPService.this, "sent : "+body, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: sent: "+body);

                            break;
                        case SEDNING : // 서버로 오류없이 보냄. tcpClient에서 sendMessage메서드를 완료함. (내가 채팅 메세지 보냄)
                            Toast.makeText(MyTCPService.this, "SEDNING", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: sending");
                            break;
                        case INFO : // 서버의 알림 메세지 도착
                            Log.d(TAG, "handleMessage: info");
                            break;
                        case SHUTDOWN : // 소켓 연결 종료
//                            Toast.makeText(MyTCPService.this, "shutdown", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: shutdown");
                            break;
                        case ERROR :
//                            Toast.makeText(MyTCPService.this, "error", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: error");
                            break;
                        default:
                            Log.d(TAG, "handleMessage: default? "+msg.what+" "+msg.obj);
                    }
                }
            };
        }

        if (tcpClient == null){
            // 여긴 언제 실행되고 언제 안되는지 잘 모르겠다
            Log.d(TAG, "onStartCommand: init tcpClient");

            String server_ip = getString(R.string.server_ip);
            String server_port = getString(R.string.server_tcp_port);

            // tcpClient는 무조건 여기서만 초기화 되어야 하므로! 여기서 getInstance가 아니라 초기화를 하자.
            tcpClient = new MyTCPClient(handler,server_ip,server_port,id); // handler, serverIp, serverPort, userId
        }
//        return super.onStartCommand(intent, flags, startId);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
//        Toast.makeText(this, "onHandleIntent "+ tcpClient.isRunning(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onHandleIntent: tcpClient.isRunning() ? "+ tcpClient.isRunning());

        if (!tcpClient.isRunning()){
            tcpClient.run();

            // 이 아래는 tcpClient에서 run() 하다가 socketException 등으로 스레드가 빠져나왔을 때 실행된다.
            Log.d(TAG, "onHandleIntent: stopSelf");
            stopSelf();
        }
    }

    public String getRid(){
        return activeRoomId;
    }

    public interface OnNewMessageReceivedListener {
        void onMessageReceivedCallback(String rid);
    }

    /**
     * should be called when an activity is bound
     * it is both for ChatActivity and RoomTab in MainActivity
     *
     * 근데 두 액티비티가 동시에 bind 되어있을 경우는 이 리스너가 둘 중 하나에만 연결될 텐데 ?
     * 현재 활성화된 액티비티에 리스너가 연결 되도록 !!
     *
     * */
    public void setOnNewMessageRecievedListener(OnNewMessageReceivedListener listener, String rid){
//        Toast.makeText(this, "set listener "+rid, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "setOnNewMessageRecievedListener: ");
        listeners.add(listener);
        activeRoomId = rid;
    }

    public void unsetOnNewMessageRecievedListener(OnNewMessageReceivedListener listener, String rid){
//        Toast.makeText(this, "unset listner"+rid, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "unsetOnNewMessageRecievedListener: ");
        listeners.remove(listener);
        if (activeRoomId.equals(rid)){
            activeRoomId = "";
        }
    }

    private void sendMyNotification(PendingIntent pendingIntent, String title, String body,String date){
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        Notification notification
                = new Notification.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.tab_talk)
                .setTicker("새로운 메세지가 도착했습니다")
                .setContentIntent(pendingIntent)
                .setSubText(date)
//                .setWhen(time)
                .build();

        notification.defaults = Notification.DEFAULT_SOUND;//소리추가
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE; //알림 소리를 한번만 내도록
        notification.flags = Notification.FLAG_AUTO_CANCEL;//확인하면 자동으로 알림이 제거 되도록

        notificationManager.notify(999, notification);
    }


}

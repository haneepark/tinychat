package com.parkhanee.tinychat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import static com.parkhanee.tinychat.MyTCPClient.CONNECTED;
import static com.parkhanee.tinychat.MyTCPClient.CONNECTING;
import static com.parkhanee.tinychat.MyTCPClient.ERROR;
import static com.parkhanee.tinychat.MyTCPClient.INFO;
import static com.parkhanee.tinychat.MyTCPClient.READY_TO_TALK;
import static com.parkhanee.tinychat.MyTCPClient.RECEIVED;
import static com.parkhanee.tinychat.MyTCPClient.SENDING;
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
 * --> tcpClient가 async에게, async가 액티비티 에게 전송 잘 되었다는 걸 알림
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
    private static MyTCPClient tcpClient=null;
    public static boolean alive = false;

    public MyTCPService(String name) {
        super(name);
    }

    public MyTCPService() {
        super("MyTCPService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (handler==null){
            Log.d(TAG, "onStartCommand: init handler");
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch (msg.what){
                        case RECEIVED :  // 친구에게서 메세지 도착
                            Log.d(TAG, "handleMessage: received");
                            // TODO: 2017. 8. 24. pref에 최근메세지 등록 + pref의 방목록에 없으면 새로운방 등록

                            // 노티 띄우기
                            Intent intent = new Intent(MyTCPService.this, Main2Activity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(MyTCPService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);


                            NotificationManager notificationManager =
                                    (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                            Notification notification
                                    = new Notification.Builder(getApplicationContext())
                                    .setContentTitle(msg.obj.toString())
                                    .setContentText("Content Text")
                                    .setSmallIcon(R.drawable.ic_add_room)
                                    .setTicker("알림!!!")
                                    .setContentIntent(pendingIntent)
                                    .build();

                            notification.defaults = Notification.DEFAULT_SOUND;//소리추가
                            notification.flags = Notification.FLAG_ONLY_ALERT_ONCE; //알림 소리를 한번만 내도록
                            notification.flags = Notification.FLAG_AUTO_CANCEL;//확인하면 자동으로 알림이 제거 되도록

                            notificationManager.notify(999, notification);
                            break;

                        case CONNECTING :
                            Toast.makeText(MyTCPService.this, "connecting", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: connecting");
                            break;
                        case CONNECTED :
                            Toast.makeText(MyTCPService.this, "connected", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: connected");
                            break;
                        case READY_TO_TALK:
                        Toast.makeText(MyTCPService.this, "ready to talk", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: ready to talk");
                            break;
                        case SENT :
                            Toast.makeText(MyTCPService.this, "sent : "+msg.obj, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: sent");
                            break;
                        case SENDING :
                            Toast.makeText(MyTCPService.this, "sending", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: sending " + msg.obj);
                            break;
                        case INFO : // 서버의 알림 메세지 도착
                            Log.d(TAG, "handleMessage: info");
                            break;
                        case SHUTDOWN : // 소켓 연결 종료
                            Toast.makeText(MyTCPService.this, "shutdown", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: shutdown");
                            break;
                        case ERROR :
                            Toast.makeText(MyTCPService.this, "error", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "handleMessage: error");
                            break;
                        default:
                            Log.d(TAG, "handleMessage: default "+msg.what+" "+msg.obj);
                    }
                }
            };
        }

        if (pref==null){
            Log.d(TAG, "onStartCommand: init pref");
            pref = MyPreferences.getInstance(this);
        }

        if (tcpClient == null){
            Log.d(TAG, "onStartCommand: init tcpClient");
            // 소켓연결 안되어있을 때 만 아래의 코드 실행.
            // handler, serverIp, serverPort, userId
            String server_ip = getString(R.string.server_ip);
            String server_port = getString(R.string.server_tcp_port);
            String id = pref.getId();
            if (id.equals("")){
                Log.e(TAG, "onStartCommand: id is empty");
            }
            tcpClient = MyTCPClient.getInstance(handler,server_ip,server_port,id);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: tcpClient.isRunning() ? "+ tcpClient.isRunning());

        alive = true;

        while (alive){
            Log.d(TAG, "onHandleIntent: while alive");

            if (!tcpClient.isRunning()){

                tcpClient.run();

                // 이 아래는 tcpClient에서 run() 하다가 socketException 등으로 스레드가 빠져나왔을 때 실행된다.
                Log.d(TAG, "onHandleIntent: stopSelf");
                stopSelf();
                alive =false;
            }
        }


    }

    public static boolean isRunning(){
        return alive;
    }

    @Override
    public boolean stopService(Intent name) {
        Log.d(TAG, "stopService: ");
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (this){
            alive = false;
        }
        tcpClient.stopClient();
        Log.d(TAG, "onDestroy: ");
    }
}

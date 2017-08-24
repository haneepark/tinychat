package com.parkhanee.tinychat;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by parkhanee on 2017. 8. 23..
 *
 * 처음 tcp 연결을 생성하고 관리하는 MyTcpAsync와 같은 역할 ! + tcpClient의 역할 !
 * tcp 연결을 생성, 유지, 서버와 데이터 주고받기.
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
 * --> 전송하고자 하는 채팅 메세지를 서비스에게 인텐트로 전달
 * --> 서비스가 해당 메세지를 서버에게 전송
 * --> 전송 잘 되었다는 걸 액티비티에 알림
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
 */

public class MyTcpClientService extends IntentService {
    public static boolean run=false;
    private static final String TAG = "MyTcpClientService";

    public MyTcpClientService(String name) {
        super(name);
    }

    public MyTcpClientService() {
        super("MyTcpClientService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String server_ip = getString(R.string.server_ip);
        String server_port = getString(R.string.server_tcp_port);
        PrintWriter out;
        BufferedReader in;

        try {
            run = true;
            InetAddress serverAddr = InetAddress.getByName(server_ip);
            Log.d(TAG, "onHandleIntent: connecting . . .");

            Socket socket = new Socket(serverAddr, Integer.parseInt(server_port));

            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d(TAG, "onHandleIntent: connected ! ");

                String incomingMessage = in.readLine();
                if (incomingMessage!=null){
                    Log.d(TAG, "onHandleIntent: "+incomingMessage);
                    out.println(TAG+" ok");
                    out.flush();
                } else {
                    out.println(TAG+" no incoming message");
                    out.flush();
                }

                out.flush();
                out.close();
                in.close();
                socket.close();
                Log.d(TAG, "onHandleIntent: connection closed");
                run = false;
            } catch (IOException e) {
                run=false;
                e.printStackTrace();
            }
        } catch (IOException e1) {
            run=false;
            e1.printStackTrace();
        }

        Log.d(TAG, "onHandleIntent: stopSelf");
        stopSelf();
    }

    public static boolean isRunning(){
        // TODO: 2017. 8. 23. check if the tcp connection is running ?
        return run;
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }
}

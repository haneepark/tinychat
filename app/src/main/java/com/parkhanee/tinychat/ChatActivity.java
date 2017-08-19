package com.parkhanee.tinychat;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Room;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {
    private Socket socket;
    private final String TAG = "ChatActivity";
    private TextView chatTextView;
    Handler clientHandler;
    EditText et;
    PrintWriter out=null;
    Context context = this;
    Room room ;

    MyPreferences pref;

    String id; // 내 아이디
    String friend_id; // 일대일 방의 친구 아이디
    String rid; // 채팅방 아이디

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (pref==null){
            pref = MyPreferences.getInstance(context);
        }

        id = pref.getString("id");
        rid = getIntent().getStringExtra("rid");
        // TODO: 2017. 8. 18. pref에서 참여자 아이디 가져오기
        room = MyUtil.initRoom(context,pref,rid);

        chatTextView = (TextView) findViewById(R.id.textView);
        et = (EditText) findViewById(R.id.et_chat);
        clientHandler = new Handler();

        new Thread(new ClientThread()).start();
    }

    public void onClick(View view) {
        try {

            // printwriter인스턴스를 writeThread에서 매번 새로 생성하는거 대신에 client스레드에서 한번 만들어서 writeThread로 전달해줌.
            // 메세지 한번 보낼 때 마다 writeThread 하나 생성 --> 메세지 보냄 --> 스레드 소멸.

            if (out !=null){ // ClientThread에서 out 객체 생성되었는지 확인
                String msg = et.getText().toString();
                WriteThread writeThread = new WriteThread(out,msg);
                new Thread(writeThread).start();
            } else {
                Toast.makeText(context, "서버와 연결 중입니다. 잠시후 다시 시도 해 주십시오", Toast.LENGTH_SHORT).show();
            }

            et.setText("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 클라이언트 소켓을 생성하고, 서버로부터 메세지를 받아서 ui를 업데이트 함
    // 소켓으로 printWriter인스턴스 생성
    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(getString(R.string.server_ip));
                socket = new Socket(serverAddr, Integer.parseInt(getString(R.string.server_tcp_port)));

                BufferedReader input;
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);

                // TODO: 2017. 8. 18. 이렇게 room_id와 id를 따로 보내면 뭐가먼저 도착할지 모름. pool 만들어서 관리 해야 .

                // send rid
                WriteThread writeThread = new WriteThread(out,rid);
                new Thread(writeThread).start();

                // send id
                writeThread = new WriteThread(out,id);
                new Thread(writeThread).start();

                while(true){
                    String read = input.readLine();
                    clientHandler.post(new ClientThread.updateUIThread(read));
                }


            } catch (IOException e1) {
                e1.printStackTrace();
                clientHandler.post(new ClientThread.toastThread());
            }

        }

        class toastThread implements Runnable { //handler
            @Override
            public void run() {
                Toast.makeText(context, "서버와의 연결에 실패했습니다", Toast.LENGTH_SHORT).show();
            }
        }

        class updateUIThread implements Runnable { //handler
            private String msg;

            public updateUIThread(String str) {
                this.msg = str;
            }

            @Override
            public void run() {
                // 택스트뷰에 있던 내용 + 새로 온 내용
                chatTextView.setText(chatTextView.getText().toString() + "\n" + msg);
            }
        }

    } //ClientThread

    // 메세지를 서버에 전송함. 전송 버튼을 누르면 실행됨.
    class WriteThread implements Runnable {

        private PrintWriter mOut;
        private String msg;

        public WriteThread(PrintWriter out,String msg) {
            this.mOut = out;
            this.msg = msg;
        }

        @Override
        public void run() {
            mOut.println(msg);// 서버에 메세지 보내기

            return; //명시적으로 쓰레드 종료
        }

    }
}

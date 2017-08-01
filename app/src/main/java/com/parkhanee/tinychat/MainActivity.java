package com.parkhanee.tinychat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    MyPreferences pref=null;
    Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (pref==null){
            pref = MyPreferences.getInstance(context);
        }

        // TODO: 2017. 8. 1.   로그아웃 버튼과 이름보이는 텍스트뷰 분리
        TextView logout = (TextView) findViewById(R.id.logout);
        logout.setText("name "+pref.getString("name"));
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (pref.logout()){
                    // TODO: 2017. 7. 27. 로그아웃 되었습니다 알림
                    Toast.makeText(context, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(context,LoginActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }
                // TODO: 2017. 7. 29. 한번 로그아웃 누르고 화면 넘어가기 전에 누르면 이쪽으로 옴. 아예 안눌리도록 처리.
                Toast.makeText(context, "SP 로그아웃 실패??", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

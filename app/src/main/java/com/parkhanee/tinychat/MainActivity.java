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
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        if (pref==null){
            pref = MyPreferences.getInstance(context);
        }

        TextView logout = (TextView) findViewById(R.id.logout);
        logout.setText("name "+pref.getString("name"));
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (pref.logout()){
                    // TODO: 2017. 7. 27. 로그아웃 되었습니다 알림
                    Intent i = new Intent(context,LoginActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }
                // TODO: 2017. 7. 29. 한번 로그아웃 누르고 화면 넘어가기 전에 누르면 이쪽으로 옴. 아예 안눌리도록 처리.
                Toast.makeText(context, "로그아웃 실패??", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

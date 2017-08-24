package com.parkhanee.tinychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class Main2Activity extends AppCompatActivity {
    Button button;
    private static final String TAG = "Main2Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        button = (Button) findViewById(R.id.service_button);

        if (MyTcpClientService.isRunning()){
            button.setText("stop service");
        } else {
            button.setText("start service");
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyTcpClientService.isRunning()){
                    // todo stop the connection thus stop service

                    button.setText("start service");
                }else {
                    // start service
                    Intent i = new Intent(Main2Activity.this, MyTcpClientService.class);
                    startService(i);
                    button.setText("stop service");
                }
            }
        });

    }
}

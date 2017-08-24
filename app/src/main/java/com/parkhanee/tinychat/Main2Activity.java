package com.parkhanee.tinychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class Main2Activity extends AppCompatActivity {
    Button button;
    private static final String TAG = "Main2Activity";
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        button = (Button) findViewById(R.id.service_button);
        intent = new Intent(Main2Activity.this,MyTCPService.class);

        if (MyTCPService.isRunning()){
            button.setText("stop service");
        } else {
            button.setText("start service");
        }

        Button button1 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean run = MyUtil.isMyServiceRunning(Main2Activity.this,MyTCPService.class);
                Toast.makeText(Main2Activity.this, ""+run , Toast.LENGTH_SHORT).show();
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyTCPService.isRunning()){ // stop service
                    Log.d(TAG, "onClick: stop service");
                    // todo stop the connection thus stop service
                    stopService(intent);
                    button.setText("start service");
                }else { // start service
                    Log.d(TAG, "onClick: start service");
                    startService(intent);
                    button.setText("stop service");
                }
            }
        });

    }
}

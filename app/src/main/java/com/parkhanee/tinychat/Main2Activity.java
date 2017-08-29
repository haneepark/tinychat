package com.parkhanee.tinychat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    Button button, button3;
    private static final String TAG = "Main2Activity";
    Intent intent;
    TextView tv;

    MyTCPService tcpService;
    boolean serviceBound = false;

    final String rid = "1000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        intent = new Intent(Main2Activity.this,MyTCPService.class);
        intent.putExtra("chatActivityBound",true);
        intent.putExtra("rid",rid);


        tv = (TextView) findViewById(R.id.tv_main2);
        tv.setOnClickListener(this);
        button = (Button) findViewById(R.id.service_button);
        if (MyTCPService.isRunning()){
            button.setText("stop service");
        } else {
            button.setText("start service");
        }
        Button button1 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(this);
        button.setOnClickListener(this);
        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);
        if (serviceBound){
            button3.setText("unbind");
        } else {
            button3.setText("bind");
        }
        findViewById(R.id.button4).setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.service_button :
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
                break;
            case R.id.button2:
                boolean run = MyUtil.isMyServiceRunning(Main2Activity.this,MyTCPService.class);
                Toast.makeText(Main2Activity.this, ""+run , Toast.LENGTH_SHORT).show();
                break;
            case R.id.button3: // bind / unbind
                if (serviceBound){ // unbind
                    unbindService(serviceConnection);
                    serviceBound = false;
                    button3.setText("bind");
                } else { // bind
                    intent.putExtra("rid",rid);
                    // TODO: 2017. 8. 29. bindService option ?
                    bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
                    serviceBound = true;
                    button3.setText("unbind");
                }
                break;
            case R.id.button4:// is it bound?
                Toast.makeText(Main2Activity.this, " "+serviceBound, Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_main2:
                tv.setText("rid "+tcpService.getRid());
                break;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyTCPService.MyBinder myBinder = (MyTCPService.MyBinder) iBinder;
            tcpService = myBinder.getService();
            // tcpClient = myBinder.getClient();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };
}

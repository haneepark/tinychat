package com.parkhanee.tinychat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by parkhanee on 2017. 8. 30..
 */

public class MyBroadCastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadCastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        Toast.makeText(context, "MyBroadCastReceiver", Toast.LENGTH_SHORT).show();
        context.startService(new Intent(context,MyTCPService.class));
    }
}

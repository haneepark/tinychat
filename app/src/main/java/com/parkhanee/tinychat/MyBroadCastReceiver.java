package com.parkhanee.tinychat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        Toast.makeText(context, "MyBroadCastReceiver connected: "+isConnected, Toast.LENGTH_SHORT).show();
        if (isConnected){
            context.startService(new Intent(context,MyTCPService.class));
        }else {
            context.stopService(new Intent(context,MyTCPService.class));
        }

    }
}

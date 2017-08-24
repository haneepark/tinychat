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

package com.parkhanee.tinychat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if !logged in ? finish() : setContentView
        // login check with shared preference

        setContentView(R.layout.activity_login);



    }
}

package com.parkhanee.tinychat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String id = getIntent().getStringExtra("friend_id");
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
    }
}

package com.parkhanee.tinychat;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddFriendActivity extends AppCompatActivity {
    final String TAG = "AddFriendActivity";
    Context context = this;
    MyPreferences pref=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        if (pref==null){
            pref = MyPreferences.getInstance(context);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // do not show default name text and instead, show the textView i included
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // do not show back button

        if (!MyUtil.IsNetworkConnected(this)) {
            // TODO: 2017. 8. 5. 인터넷 경고
            Toast.makeText(this, "인터넷 연결 안됨", Toast.LENGTH_SHORT).show();
        } else {
            getAllUserRequested();
        }

    }

    public void getAllUserRequested () {

        RequestQueue queue = MyVolley.getInstance(this.getApplicationContext()).
                getRequestQueue();
        String id = pref.getString("id");
        String appendUrl = "get_all_user.php";

        String url = getString(R.string.server)+appendUrl+"?id="+id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");

                    // result code 확인
                    if (!resultCode.equals("100")){
                        Toast.makeText(context, "result "+resultCode+" "+result, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "getAllUserRequested : "+resultCode+" "+result);
                        return;
                    }

                    // 결과 처리

//                    String name = jsonObject.getString("name");
//                    String id = jsonObject.getString("id");
//                    String created = jsonObject.getString("created");
//
//                    String jsonString = "";
//                    jsonString += "name: " + name + "\n\n";
//                    jsonString += "id: " + id + "\n\n";
//                    jsonString += "created: " + created + "\n\n";
//
//                    Log.d(TAG, "onResponse:"+jsonString);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "volley error", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onErrorResponse: "+error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }
}

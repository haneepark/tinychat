package com.parkhanee.tinychat;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.parkhanee.tinychat.classbox.Friend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddFriendActivity extends AppCompatActivity {
    final String TAG = "AddFriendActivity";
    Context context = this;
    MyPreferences pref=null;
    AddFriendAdapter adapter;

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

        adapter = new AddFriendAdapter(context);
        ListView listView = (ListView) findViewById(R.id.listview_add_friend);
        listView.setAdapter(adapter);

        final EditText et_search = (EditText) findViewById(R.id.et_search);
//        et_search.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                adapter.getFilter().filter(charSequence.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//
        ImageButton btn_search = (ImageButton) findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.getFilter().filter(et_search.getText().toString());
            }
        });
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

                    Toast.makeText(context, "100", Toast.LENGTH_SHORT).show();

                    // TODO jsonObject "users" 를 받고
                    // 어레이인지 검사하고 어레이가 아니면
                        // 또는 내용물이 없나 ?     확인해서 처리하고
                    // 어레이 이면 리스트뷰로 넘겨서 처리하기 . 한 명일때도 어레이네 !
                    Object users = jsonObject.get("users");

                    if (users instanceof JSONArray) { // It's an array
                        JSONArray usersJsonArray = (JSONArray)users;
                        Toast.makeText(context, "array", Toast.LENGTH_SHORT).show();
                        // TODO: 2017. 8. 8. list view 처리
                        int length = usersJsonArray.length();
                        ArrayList<Friend> friends = new ArrayList<>();
                        for (int i=0;i<length;i++){
                            JSONObject user = (JSONObject) usersJsonArray.get(i);
                            String img;
                            if (user.has("img")){
                                img = user.getString("img");
                            } else {
                                img = "";
                            }

                            friends.add(
                                    new Friend(
                                            user.getString("id"),
                                            user.getString("nid"),
                                            user.getString("name"),
                                            img,
                                            user.getInt("created")
                                    )
                            );
                        }
                        adapter.setFriends(friends);
                        adapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(context, "불러올 유저 없음 에러 ??? ", Toast.LENGTH_SHORT).show();
//                        if (users instanceof JSONObject) { // It's an object
//                        Toast.makeText(context, "object", Toast.LENGTH_SHORT).show();
//                        JSONObject userObject = (JSONObject)users;
//
//                    } else {
                        // It's something else, like a string or number
                    }

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

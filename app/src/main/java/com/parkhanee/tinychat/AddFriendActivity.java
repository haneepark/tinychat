package com.parkhanee.tinychat;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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
    EditText et_search;
    ImageButton btn_clear;

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

        et_search = (EditText) findViewById(R.id.et_search);
        ImageButton btn_search = (ImageButton) findViewById(R.id.btn_search);

        // edit text 에서 키보드 enter 누른 경우
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    search();
                }
                return true;
            }
        });

        // search image button 누른 경우
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        // cleanable edittext
        btn_clear = (ImageButton) findViewById(R.id.cleanable_button_clear);
        btn_clear.setVisibility(View.INVISIBLE);
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
            }
        });


        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.length() > 0) {
                    btn_clear.setVisibility(View.VISIBLE);
                } else {
                    // 입력했던 글자 모두 지우면
                    // cleanable button 안보이기 && 이전 검색 결과 clear
                    btn_clear.setVisibility(View.INVISIBLE);
                    adapter.clearItem();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void search(){ // when search action is called
        String nid = et_search.getText().toString();
        if (MyUtil.nidFormChecker(nid)){
            adapter.getFilter().filter(nid);
        } else {
            // TODO: 2017. 8. 8. 경고
            Toast.makeText(context, "전화번호를 정확히 입력해 주세요", Toast.LENGTH_SHORT).show();
            // 검색결과 초기화 하기
            adapter.clearItem();
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
                        adapter.setAllFriends(friends);
                        adapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(context, "불러올 유저 없음 에러 ??? ", Toast.LENGTH_SHORT).show();
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

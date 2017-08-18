package com.parkhanee.tinychat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.Map;

public class AddFriendActivity extends AppCompatActivity {
    final String TAG = "AddFriendActivity";
    Context context = this;
    MyPreferences pref=null;
    MySQLite db=null;
    AddFriendAdapter adapter;
    EditText et_search;
    ImageButton btn_clear, btn_search;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        if (pref==null){
            pref = MyPreferences.getInstance(context);
        }
        if (db==null){
            db = MySQLite.getInstance(context);
        }

        setToolbar();

        if (!MyUtil.IsNetworkConnected(this)) {
            // TODO: 2017. 8. 5. 경고
            Toast.makeText(this, "인터넷 연결 안됨", Toast.LENGTH_SHORT).show();
        } else {
            getAllUserRequested();
        }

        adapter = new AddFriendAdapter(context);
        ListView listView = (ListView) findViewById(R.id.listview_add_friend);
        listView.setAdapter(adapter);

        setSearchViews();

    }

    public void setSearchViews(){
        et_search = (EditText) findViewById(R.id.et_search);
        btn_search = (ImageButton) findViewById(R.id.btn_search);
        btn_clear = (ImageButton) findViewById(R.id.cleanable_button_clear);
        btn_clear.setVisibility(View.INVISIBLE);

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

    public void setToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // do not show default name text and instead, show the textView i included
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // do not show back button
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
        if (queue==null){
            queue = MyVolley.getInstance(this.getApplicationContext()).
                    getRequestQueue();
        }

        String id = pref.getString("id");

        String url = getString(R.string.server_url)+getString(R.string.server_getAllUser)+"?id="+id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getAllUserRequested onResponse : "+response);

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
//                        Toast.makeText(context, "array", Toast.LENGTH_SHORT).show();

                        // jsonArray 정보를 받아서 listView에 allFriends 전달
                        int length = usersJsonArray.length();
                        ArrayList<Friend> friends = new ArrayList<>();
                        HashMap<String, String> thumbs = new HashMap<>();
                        for (int i=0;i<length;i++){
                            JSONObject user = (JSONObject) usersJsonArray.get(i);
                            String img="";
                            String thumb="";
                            if (user.has("img")){
                                img = user.getString("img");
                                if (img.length()>3){ // img 길이가 3 이상이면
                                    thumb = user.getString("thumb_url");
                                    thumbs.put(user.getString("id"),thumb); // thumbs hashmap에는 썸네일이 있을 때 만 넣음
                                }
                            }

                            // TODO: 2017. 8. 16. 친구추가하기 전에는 blob가지고 있을 필요 없음 처리 ?
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

                        adapter.setAllFriends(friends,thumbs);
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

    public class AddFriendAdapter extends BaseAdapter {
        private ArrayList<Friend> friends = new ArrayList<>();
        private ArrayList<Friend> allFriends;
        private HashMap<String, String> thumbs; // friend_id -> thumbnail url of all friends
        private Context context = null;
        private final String TAG = "AddFriendAdapter";


        public AddFriendAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return friends.size();
        }

        @Override
        public Object getItem(int i) {
            return friends.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

//        public void setFriends(ArrayList<Friend> friends) {
//            this.friends = friends;
//            if (allFriends == null) {
//                allFriends = new ArrayList<>(this.friends);
//            }
//        }

        public void setAllFriends(ArrayList<Friend> allFriends,HashMap<String, String> thumbs) {
            this.allFriends = allFriends;
            this.thumbs = thumbs;
        }

        public void clearItem() {
            friends.clear();
            this.notifyDataSetChanged();
        }

        @Override
        public View getView(int i, View v, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            final ViewHolder holder;
            if (v == null) {
                holder = new ViewHolder();
                v = inflater.inflate(R.layout.listview_add_friend, null);
                holder.name = (TextView) v.findViewById(R.id.add_friend_name);
                holder.img = (ImageView) v.findViewById(R.id.add_friend_img);
                holder.add = (ImageButton) v.findViewById(R.id.add_friend_button);
                holder.db = MySQLite.getInstance(context);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag(); // we call the view created before to not create a view in each time
            }

            if (friends.size() > 0) {
                final Friend friend = friends.get(i);
                holder.name.setText(friend.getName());

                // TODO: 2017. 8. 8. set image

                // set onClickListener on "add" button
                holder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // 네트워크 연결되어있고 해당 유저가 친구로 등록되어있지 않음
                        if (!MyUtil.IsNetworkConnected(context)){
                            // TODO: 2017. 8. 9. 경고
                            Toast.makeText(context, "인터넷 없음", Toast.LENGTH_SHORT).show();
                        } else {
                            if (db.getFriend(friend.getId()) != null){
                                // TODO: 2017. 8. 8. 경고
                                Toast.makeText(context, "이미 로컬db에서 친구입니다", Toast.LENGTH_SHORT).show();
                            }// TODO: 2017. 8. 12. 로컬에서 친구여도 서버로 가서 디비등록 하도록 해놓음

                            //  add friend to server database

                            String thumb_url = thumbs.get(friend.getId());
                            onAddFriendRequested(friend,thumb_url);

                        }
                    }
                });

            }

            return v;
        }


        private class ViewHolder {
            TextView name = null;
            ImageView img = null;
            ImageButton add = null;
            MySQLite db = null;
        }

        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                    ArrayList<Friend> FilteredArrList = new ArrayList<>();

                    if (allFriends == null) {
                        allFriends = new ArrayList<>(friends);
                    }

                    /********
                     *
                     *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                     *  else does the Filtering and returns FilteredArrList(Filtered)
                     *
                     ********/
                    if (constraint == null || constraint.length() == 0) {

                        // set the Original result to return
                        results.count = allFriends.size();
                        results.values = allFriends;
                    } else {
                        constraint = constraint.toString().toLowerCase();
                        Log.d(TAG, "performFiltering: constraint " + constraint);
                        Log.d(TAG, "performFiltering: allFriend.size" + String.valueOf(allFriends.size()));
                        for (int i = 0; i < allFriends.size(); i++) {
                            Friend f = allFriends.get(i);
                            String data = f.getNid();
                            Log.d(TAG, "performFiltering: data " + data);
                            if (data.toLowerCase().equals(constraint.toString())) { //startsWith(constraint.toString())) {
                                FilteredArrList.add(
                                        new Friend(f.getId(), f.getNid(), f.getName(), f.getImgUrl(), f.getCreated()
                                        )
                                );
                            }
                        }
                        // set the Filtered result to return
                        results.count = FilteredArrList.size();
                        results.values = FilteredArrList;
                    }

                    return results;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    friends = (ArrayList<Friend>) filterResults.values;
                    if (friends.size() == 0) {
                        // TODO: 2017. 8. 8. 알림
                        Toast.makeText(context, " 일치하는 결과 없음", Toast.LENGTH_SHORT).show();
                    }
                    Log.d(TAG, "publishResults: " + friends.toString());
                    notifyDataSetChanged();
                }
            };
            return filter;
        }
    }

    public void onAddFriendRequested (final Friend friend,final String thumb_url) {
        final String TAG = "onAddFriendRequested";
        final String friendId = friend.getId();

        if (queue==null){
            queue = MyVolley.getInstance(this.getApplicationContext()).
                    getRequestQueue();
        }
        final String id = pref.getString("id");

        String url = getString(R.string.server_url)+getString(R.string.server_addFriend);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,"onAddFriendRequested response : "+ response);

                try {

                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");

                    // result code 확인
                    if (resultCode.equals("302")&jsonObject.getJSONArray("followings:"+id).toString().contains(friendId)){
                        // TODO: 2017. 8. 9. 알림
                        Toast.makeText(context, "이미 서버db에서 친구입니다", Toast.LENGTH_SHORT).show();
                        if (db.getFriend(friend.getId()) == null){

                            // local db에 친구관계 추가
                            db.addFriend(friend);
                            Toast.makeText(context, "local db에 친구 등록 완료", Toast.LENGTH_SHORT).show();

                            // TODO: 2017. 8. 16. get thumbnail path
                            if (thumb_url!=null){
                                Log.d(TAG, "onResponse: Sending Thumbnail request");
                                onGetThumbnailRequested(friend, thumb_url);
                            }

                        }
                    } else if (!resultCode.equals("100")){
                        // TODO: 2017. 8. 9. 실패 경고
                        Toast.makeText(context, "result "+resultCode+" "+result, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "getAllUserRequested : "+resultCode+" "+result);
                        return;
                    } else {
                        Toast.makeText(context, "100", Toast.LENGTH_SHORT).show();

                        // local db에 친구관계 추가
                        db.addFriend(friend);
                        // TODO: 2017. 8. 16. get thumbnail path
                        if (thumb_url!=null){
                            Log.d(TAG, "onResponse: Sending Thumbnail request");
                            onGetThumbnailRequested(friend, thumb_url);
                        }

                        // TODO: 2017. 8. 9. 친구등록 완료 알림
                        Toast.makeText(context, "친구 등록 완료", Toast.LENGTH_SHORT).show();
                    }

                    // 1 메인 액티비티로 가고 나서 새로 등록된 친구 바로 보여야 함
                    // 2 액티비티 스텍에 원래 원래 있던 메인 액티비티는 없어지거나 병합되어야 함
                    // CLEAR_TOP : 스택 아래에 있는 메인액티비티의 onCreate로 가면서 위에 있는 액티비티는 싹 밀어버림.
                    Intent i = new Intent(context,MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();

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
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                Log.d(TAG, "getParams: id f_id"+id+" "+friendId);
                params.put("my_id",id);
                params.put("friend_id",friendId);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded"); //form ?
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }

    public void onGetThumbnailRequested (final Friend friend, final String thumb_url) {
        final String TAG = "onGetThumbnailRequested";

        if (queue==null){
            queue = MyVolley.getInstance(this.getApplicationContext()).
                    getRequestQueue();
        }
//        final String id = pref.getString("id");

        String url = getString(R.string.server_url)+getString(R.string.server_getThumbnail)+"?thumb_url="+thumb_url;

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,"onGetThumbnailRequested response : "+ response);

                try {

                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");

                    if (resultCode.equals("100") & jsonObject.has("thumb_blob")) {
                        // TODO: 2017. 8. 16. 성공 알림
                        Toast.makeText(context, "thumb 100", Toast.LENGTH_SHORT).show();
                        // TODO: 2017. 8. 16. save thumbnail on SQLite
                        byte[] bytes = Base64.decode(jsonObject.getString("thumb_blob"), Base64.DEFAULT);
                        friend.setImgBlob(bytes);
                        db.updateFriend(friend);

                    } else {
                        // TODO: 2017. 8. 9. 실패 경고
                        Toast.makeText(context, "thumb "+resultCode+" "+result, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onGetThumbnailRequested : "+resultCode+" "+result);
                        return;
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
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded"); //form ?
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }


}

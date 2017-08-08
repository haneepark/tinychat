package com.parkhanee.tinychat;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.parkhanee.tinychat.classbox.Friend;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by parkhanee on 2017. 8. 8..
 */

public class AddFriendAdapter_old extends BaseAdapter {
    private ArrayList<Friend> friends = new ArrayList<>();
    private ArrayList<Friend> allFriends;
    private Context context=null;
    private final String TAG = "AddFriendAdapter_old";


    public AddFriendAdapter_old(Context context){
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

    public void setFriends(ArrayList<Friend> friends) {
        this.friends = friends;
        if (allFriends==null){
            allFriends = new ArrayList<>(this.friends);
        }
    }

    public void setAllFriends(ArrayList<Friend> allFriends) {
        this.allFriends = allFriends;
    }

    public void clearItem(){
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

        if (friends.size()>0){
            final Friend friend = friends.get(i);
            holder.name.setText(friend.getName());

            // TODO: 2017. 8. 8. set image

            // set onClickListener on "add" button

/*
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (MyUtil.IsNetworkConnected(context)){

                        // TODO: 2017. 8. 8. add friend to server database
                        onAddFriendRequested(friend);


                    } else {
                        // TODO: 2017. 8. 8. 경고
                        Toast.makeText(context, "인터넷 없음", Toast.LENGTH_SHORT).show();
                    }
                }
            });
*/
        }

        return v;
    }



    private static class ViewHolder {
        TextView name = null;
        ImageView img = null;
        ImageButton add = null;
        MySQLite db = null;
    }

    public Filter getFilter(){
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Friend> FilteredArrList = new ArrayList<>();

                if (allFriends==null){
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
                    Log.d(TAG, "performFiltering: constraint "+constraint);
                    Log.d(TAG, "performFiltering: allFriend.size"+String.valueOf(allFriends.size()));
                    for (int i = 0; i < allFriends.size(); i++) {
                        Friend f = allFriends.get(i);
                        String data = f.getNid();
                        Log.d(TAG, "performFiltering: data "+data);
                        if (data.toLowerCase().equals(constraint.toString())){ //startsWith(constraint.toString())) {
                            FilteredArrList.add(
                                    new Friend(f.getId(),f.getNid(),f.getName(),f.getImg(),f.getCreated()
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
                friends = (ArrayList<Friend>) filterResults.values ;
                if (friends.size()==0){
                    // TODO: 2017. 8. 8. 알림
                    Toast.makeText(context, " 일치하는 결과 없음", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "publishResults: "+friends.toString());
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    /*

    public void onAddFriendRequested (final Friend friend) {
        String appendUrl = "add_friend.php";

        RequestQueue queue = MyVolley.getInstance(context.getApplicationContext()).
                getRequestQueue();

        String url = context.getString(R.string.server)+appendUrl;

        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    // result code 확인
                    if (!resultCode.equals("100")){
                        // TODO: 2017. 7. 29.  경고
                        Toast.makeText(context,"친구 추가 실패, code : " + resultCode+", result : "+result, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(context, "친구추가 result "+resultCode+" "+result, Toast.LENGTH_SHORT).show();

                    // TODO: 2017. 8. 8. add friend to local database
                    holder.db.addFriend(friend);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent i = new Intent(context,MainActivity.class);
                context.startActivity(i);
                // TODO: 2017. 8. 8. finish AddFriendActivity ? 아니면 메인액티비로 넘어가는 코드가 여기가 아니라 AddFriendActivity에서 실행되어야 하나 ?
                // TODO: 2017. 8. 8. friendTab으로 가면서 친구목록 다시 불러오기 !! 친구 추가되었으니까.

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: 2017. 7. 26. 로그인에 실패했습니다 아이디와 비밀번호를 확인해주세요 경고
                Toast.makeText(context, "로그인에 실패 했습니다 서버 에러?", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onErrorResponse: "+error.getMessage());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                // shared preferences 에서 내정보도 가져와야 함
                params.put("id",)
                params.put("nid",nid);
                params.put("pwd",pwd);
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
    */

}

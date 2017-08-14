package com.parkhanee.tinychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parkhanee.tinychat.classbox.Friend;

import java.util.ArrayList;

/**
 * Created by parkhanee on 2017. 8. 4..
 */

public class FriendTabAdapter extends BaseAdapter {
    final String TAG = "FriendTabAdapter";
    private ArrayList<Friend> friendArrayList = new ArrayList<>();
    private Context context=null;


    public FriendTabAdapter(Context context){
        this.context = context;
//        setDummy();
    }

    private void setDummy(){
//        friendArrayList.add(new Room("123345",1));
    }

    @Override
    public int getCount() {
        return friendArrayList.size();
    }

    @Override
    public Friend getItem(int i) {
        return friendArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void addItem(Friend friend){
        friendArrayList.add(friend);
    }

    public void clearItem(){
        friendArrayList.clear();
    }

    public void addItem(Friend friend, int position){
        friendArrayList.add(position,friend);
    }

    public void setFriendArrayList(ArrayList<Friend> friends){
        friendArrayList = friends;
    }

    @Override
    public View getView(int position, View v, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final ViewHolder holder;
        if (v == null) {
            holder = new ViewHolder();
            v = inflater.inflate(R.layout.listview_friend, null);
            holder.name = (TextView) v.findViewById(R.id.friend_name);
            holder.img = (ImageView) v.findViewById(R.id.add_friend_img);
            holder.dialog = new UserProfileDialog.Builder(context);
            holder.layout = v.findViewById(R.id.friend_item);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag(); // we call the view created before to not create a view in each time
        }

        if (friendArrayList.size()>0){ // 친구 존재 할 때
            // 친구 목록 리스트뷰 아이템
            Friend friend = friendArrayList.get(position);
            holder.name.setText(friend.getName());
            // 각 아이템의 프로필 다이알로그 설정
            holder.dialog.setTextName(friend.getName())
                    .setTextNumber(friend.getNid())
                    .setMine(false)
                    .setImageUrl(friend.getImg())
                    .setFriend_id(friend.getId())
                    .build();

            // show friend's profile dialog
            // 원래는 FriendTabFragment 에서 listView.OnItemClickListener 를 써야 하는데 대신에 리스트뷰 아이템 감싸는 레이아웃을 불러서 온클릭리스너 등록
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.dialog.show();
                }
            });
        }

        return v;
    }

    private static class ViewHolder {
        TextView name = null;
        ImageView img = null;
        UserProfileDialog.Builder dialog=null;
        View layout = null;
    }
}

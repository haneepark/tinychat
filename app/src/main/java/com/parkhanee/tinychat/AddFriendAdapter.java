package com.parkhanee.tinychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.parkhanee.tinychat.classbox.Friend;

import java.util.ArrayList;

/**
 * Created by parkhanee on 2017. 8. 8..
 */

public class AddFriendAdapter extends BaseAdapter {
    private ArrayList<Friend> friendArrayList = new ArrayList<>();
    private Context context=null;
    private final String TAG = "AddFriendAdapter";

    public AddFriendAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return friendArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return friendArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setFriendArrayList(ArrayList<Friend> friendArrayList) {
        this.friendArrayList = friendArrayList;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder;
        if (v == null) {
            holder = new ViewHolder();
            v = inflater.inflate(R.layout.listview_add_friend, null);
            holder.name = (TextView) v.findViewById(R.id.add_friend_name);
            holder.img = (ImageView) v.findViewById(R.id.add_friend_img);
            holder.add = (ImageButton) v.findViewById(R.id.add_friend_button);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag(); // we call the view created before to not create a view in each time
        }

        // TODO: 2017. 8. 8. set image, and set onClickListener
        holder.name.setText(friendArrayList.get(i).getName());


        return v;
    }

    private static class ViewHolder {
        TextView name = null;
        ImageView img = null;
        ImageButton add = null;
    }
}

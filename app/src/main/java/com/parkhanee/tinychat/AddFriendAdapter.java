package com.parkhanee.tinychat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;

import com.parkhanee.tinychat.classbox.Friend;

import java.util.ArrayList;

/**
 * Created by parkhanee on 2017. 8. 8..
 */

public class AddFriendAdapter extends BaseAdapter {
    private ArrayList<Friend> friends = new ArrayList<>();
    private ArrayList<Friend> allFriends;
    private Context context=null;
    private final String TAG = "AddFriendAdapter";

    public AddFriendAdapter(Context context){
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

    public void clearItem(){
        friends.clear();
        this.notifyDataSetChanged();
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

        if (friends.size()>0){
            Friend friend = friends.get(i);
            holder.name.setText(friend.getName());

            // TODO: 2017. 8. 8. set image, and set onClickListener

        }

        return v;
    }

    private static class ViewHolder {
        TextView name = null;
        ImageView img = null;
        ImageButton add = null;
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
                        String data = f.getName();
                        Log.d(TAG, "performFiltering: data "+data);
                        if (data.toLowerCase().contains(constraint.toString())){ //startsWith(constraint.toString())) {
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
                Log.d(TAG, "publishResults: "+friends.toString());
                notifyDataSetChanged();
            }
        };
        return filter;
    }
}

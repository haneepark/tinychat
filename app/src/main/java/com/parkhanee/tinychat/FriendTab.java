package com.parkhanee.tinychat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


public class FriendTab extends Fragment {
    final String TAG = "FriendTab";
    private FriendTabAdapter adapter;
    private ViewGroup header;
    MySQLite db = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment_friend,container,false);
        header = (ViewGroup)inflater.inflate(R.layout.listview_header_friend, container, false);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db==null){
            db = MySQLite.getInstance(getActivity().getApplicationContext());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new FriendTabAdapter(getActivity());
        ListView listView = (ListView) view.findViewById(R.id.friend_list_view);
        listView.setAdapter(adapter);
        listView.addHeaderView(header, null, false);
        adapter.setFriendArrayList(db.getAllFriends());
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // TODO: 2017. 8. 4.
            }
        });
    }
}

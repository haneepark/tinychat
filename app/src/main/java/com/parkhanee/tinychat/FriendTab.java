package com.parkhanee.tinychat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Friend;


public class FriendTab extends Fragment implements View.OnClickListener {
    final String TAG = "FriendTab";
    private FriendTabAdapter adapter;
    private ViewGroup header;
    MySQLite db = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment_friend,container,false);
        header = (ViewGroup)inflater.inflate(R.layout.listview_header_friend, container, false);
        header.findViewById(R.id.myprofile).setOnClickListener(this); // header안에 있는 애니까 header에서 찾아줌 !!
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
                // TODO: 2017. 8. 4. show friend's profile dialog
                Friend friend = (Friend) adapterView.getItemAtPosition(i);
                Toast.makeText(getActivity(), "clicked "+friend.getName()+" id:"+friend.getId(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.myprofile :
                // TODO: 2017. 8. 4. show my profile dialog
                Toast.makeText(getActivity(), "my profile", Toast.LENGTH_SHORT).show();
                break;

        }
    }
}

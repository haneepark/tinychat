package com.parkhanee.tinychat;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Room;

import java.util.ArrayList;

/**
 * Created by parkhanee on 2017. 8. 3..
 */

public class RoomTab extends Fragment {
    private final String TAG = "RoomTab";
    private RoomTabAdapter adapter;
    MySQLite db=null;
    MyPreferences pref = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment_room,container,false);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db==null){
            db = MySQLite.getInstance(getActivity().getApplicationContext());
        }
        if (pref==null){
            pref = MyPreferences.getInstance(getActivity());
        }
;    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new RoomTabAdapter(getActivity());
        ListView listView = (ListView) getActivity().findViewById(R.id.room_list_view);
        listView.setAdapter(adapter);
        /*
        * listview의 어레이아이템 추가/삭제는 getView 안에서 할 수 없다!
        * 아이템의 개수가 먼저 정해지고 나서 getView가 실행되고, getView안에서 각각의 아이템에 대하여 position 변수가 존재하기 때문이다.
        * getView의 역할 자체가 아이템 하나하나를 inflate하는 건데, getView하기전에 적어도 아이템이 몇개가 있을지는 결정되어있어야지.
        * 아이템 개수는 사실 Adapter의 getCount매써드에서 정할수도 있긴 하지만..
        * */
        ArrayList<Room> roomArrayList = db.getAllRooms();
        for (Room room : roomArrayList){
            room.setParticipants(pref.getString(room.getRid()),getActivity());
        }
        adapter.setRoomArrayList(roomArrayList);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // TODO: 2017. 8. 4. go to ChatActivity
                Room room = (Room)adapterView.getItemAtPosition(i);
                Toast.makeText(getActivity(), "clicked "+room.getRid(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}

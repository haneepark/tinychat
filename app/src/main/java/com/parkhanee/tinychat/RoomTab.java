package com.parkhanee.tinychat;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Room;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by parkhanee on 2017. 8. 3..
 */

public class RoomTab extends Fragment implements View.OnClickListener {
    private final String TAG = "RoomTab";
    private RoomTabAdapter adapter;
    MySQLite db = null;
    MyPreferences pref = null;
    Context context = getActivity();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_fragment_room, container, false);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db == null) {
            db = MySQLite.getInstance(getActivity().getApplicationContext());
        }
        if (pref == null) {
            pref = MyPreferences.getInstance(getActivity());
        }
    }

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
//        ArrayList<Room> roomArrayList = db.getAllRooms();

        pref.putString("rid","2:1,232,2");
        pref.putString("ppl1","1:91433734");
        pref.putString("ppl232","2:11111111,91433734");
        pref.putString("ppl2","1:11111111");

        if (pref.contains("rid")){ // 채팅 방 존재
               /*
               *   rid - 충방개수:rid1,rid2,rid3,  . . .
               *    e.g.) rid - 3:345,232,222
               *    pref string 에서 roomArrayList 만들기
               */
            String string = pref.getString("rid"); // string = "3:345,343,222"
            String[] strings = string.split(":"); // strings = [ "3" , "345:343:333" ]
            ArrayList<String> arrayList =  new ArrayList<>(Arrays.asList(strings[1].split(",")));

            Log.d(TAG, "onViewCreated: 방 개수 "+strings[0]+" == "+arrayList.size());

            ArrayList<Room> roomArrayList = new ArrayList<>();
            for (String rid : arrayList){ // 방 개수만큼 반복.
                Room room;

                //ppl345 -  2:68620823,11111111
                String pplString = pref.getString("ppl"+rid); // pplString = "2:68620823,11111111"
                String[] pplStrings = pplString.split(":"); // pplStrings = [ "2" , "68620823,11111111" ]

                room = new Room(rid,Integer.parseInt(pplStrings[0]),pplStrings[1],context);
                roomArrayList.add(room);
            }

            adapter.setRoomArrayList(roomArrayList);
            adapter.notifyDataSetChanged();
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO: 2017. 8. 4. go to ChatActivity
//                Room room = (Room) adapterView.getItemAtPosition(position);
//                Toast.makeText(getActivity(), "clicked " + room.getRid(), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getActivity(),ChatActivity.class);
                i.putExtra("rid",((Room)adapterView.getItemAtPosition(position)).getRid());
                startActivity(i);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }
}

package com.parkhanee.tinychat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parkhanee.tinychat.classbox.Chat;
import com.parkhanee.tinychat.classbox.Room;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by parkhanee on 2017. 8. 3..
 */

public class RoomTab extends Fragment implements View.OnClickListener, MyTCPService.OnNewMessageReceivedListener {
    private final String TAG = "RoomTab";
    private RoomTabAdapter adapter;
    MySQLite db = null;
    MyPreferences pref = null;

    boolean serviceBound=false;
    MyTCPService tcpService;

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // go to ChatActivity
                Intent i = new Intent(getActivity(),ChatActivity.class);
                i.putExtra("rid",((Room)adapterView.getItemAtPosition(position)).getRid());
                i.putExtra("isPrivate",((Room)adapterView.getItemAtPosition(position)).isPrivate());
                startActivity(i);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = new Intent(getActivity(),MyTCPService.class);
        getActivity().bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
        serviceBound = true;

        // TODO: 2017. 8. 26. 채팅방 늘어나면 이 작업 다시 하기 위해서 onViewCreated 에 있던걸 여기로 옮김 !!
        // onResume은 MainActivity가 백스택에 있다가 focus를 새로 받을 때 마다 실행 된다.
        if (pref.contains("rooms")){ // 채팅 방 존재

            ArrayList<Room> roomArrayList = pref.getAllRooms();

            if (roomArrayList!=null){
                HashMap<String,Chat> chatHashMap = db.getRecentChatInRooms(roomArrayList);
                if (chatHashMap!=null){
                    adapter.setRoomArrayList(roomArrayList,chatHashMap);
                    adapter.notifyDataSetChanged();
                }else {
                    Log.e(TAG, "onResume: recent chat is null");
                }
            } else {
                Log.e(TAG, "onResume: there is no room");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (serviceBound){
            Log.d(TAG, "onStop: unbindService");
            getActivity().unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    public void onMessageReceivedCallback(String rid) {// refresh room ListView!

        // FIXME : 2017. 9. 2. rid 에 해당하는 방의 최근채팅, 방 프린트 순서 "만" 업데이트 하면 됨.

        if (pref.contains("rooms")){ // 채팅 방 존재

            ArrayList<Room> roomArrayList = pref.getAllRooms();

            if (roomArrayList!=null){
                HashMap<String,Chat> chatHashMap = db.getRecentChatInRooms(roomArrayList);
                if (chatHashMap!=null){
                    adapter.setRoomArrayList(roomArrayList,chatHashMap);
                    adapter.notifyDataSetChanged();
                }else {
                    Log.e(TAG, "onResume: recent chat is null");
                }
            } else {
                Log.e(TAG, "onResume: there is no room");
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyTCPService.MyBinder myBinder = (MyTCPService.MyBinder) iBinder;
            tcpService = myBinder.getService();
            tcpService.setOnNewMessageRecievedListener(RoomTab.this,null);
            // tcpClient = myBinder.getClient();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            tcpService.unsetOnNewMessageRecievedListener(RoomTab.this);
            serviceBound = false;
        }
    };
}

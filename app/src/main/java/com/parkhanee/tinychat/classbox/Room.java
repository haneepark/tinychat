package com.parkhanee.tinychat.classbox;

import android.content.Context;
import android.util.Log;

import com.parkhanee.tinychat.MySQLite;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by parkhanee on 2017. 8. 2..
 */

public class Room {
    String TAG = "Room";
    private String rid;
    private int ppl;
    private Boolean isPrivateRoom=null;
    private ArrayList<Friend> participants = new ArrayList<>();
    private MySQLite db =null;

    // TODO: 2017. 8. 2. 최근 대화 sharedPreferences는 어디서 뽑지?

    public Room(String rid, int ppl){
        this.rid = rid;
        this.ppl = ppl;
        this.isPrivateRoom = isPrivateRoom();
    }

    public Boolean isPrivateRoom(){
        if (isPrivateRoom==null){
            isPrivateRoom = ppl == 1;
        }
        return isPrivateRoom;
    }

    public String getRid() {
        return rid;
    }

    public int getPpl() {
        return ppl;
    }

    public ArrayList<Friend> getParticipants() {
        return participants;
    }

    public boolean setParticipants (String string, Context context) {

        if (db==null){
            db = MySQLite.getInstance(context);
        }

        // pplListString에 들어있는 id 쪼개서 어레이리스트에 넣기
        ArrayList<String> arrayList =  new ArrayList<>(Arrays.asList(string.split(":")));
        if (arrayList.size()!=ppl){
            Log.d(TAG, "setParticipants: participants.size()!=ppl ");
            return false;
        }

        for (String id : arrayList){
            participants.add(db.getFriend(id));
        }
        return true;
    }

    @Override
    public String toString() {
        return "Room{" +
                "rid='" + rid + '\'' +
                ", ppl=" + ppl +
                ", isPrivateRoom=" + isPrivateRoom +
                '}';
    }
}

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
    private MySQLite db =null;
    private Context context;

    private ArrayList<Friend> participants = new ArrayList<>(); // 여러명방일때
    private Friend participant; // 일대일방일때



    // TODO: 2017. 8. 2. 최근 대화 sharedPreferences는 어디서 뽑지?

    /**
     * @param string 참여자 목록을 콤마(,)로 구분해서 나열한 String
     * */
    public Room(String rid, int ppl,String string, Context context){
        this.rid = rid;
        this.ppl = ppl;
        this.context = context;
        initRoom(string);
    }

    public Boolean isPrivate(){
        return isPrivateRoom;
    }

    public String getRid() {
        return rid;
    }

    public int getPpl() {
        return ppl;
    }

    public ArrayList<Friend> getParticipants() {
        if (ppl>1){
            return participants;
        } else {
            return null;
        }
    }

    public Friend getParticipant() {
        if (ppl==1){
            return participant;
        }else {
            return null;
        }

    }

    public boolean initRoom(String string) {

        if (db==null){
            db = MySQLite.getInstance(context);
        }

        if (ppl==1){ // 일대일 방

            isPrivateRoom = true;
            participant = db.getFriend(string);

        }else { // 멀티방

            isPrivateRoom = false;

            // pplListString에 들어있는 id 쪼개서 어레이리스트에 넣기
            ArrayList<String> arrayList =  new ArrayList<>(Arrays.asList(string.split(",")));
            if (arrayList.size()!=ppl){
                Log.d(TAG, "initRoom: participants.size()!=ppl ");
                return false;
            }

            for (String id : arrayList){
                participants.add(db.getFriend(id));
            }
        }

        Log.d(TAG, "initRoom: "+ this.toString());

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

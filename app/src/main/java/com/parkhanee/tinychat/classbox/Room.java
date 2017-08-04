package com.parkhanee.tinychat.classbox;

import android.util.Log;

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
    private ArrayList<String> pplArrayList = new ArrayList<>();

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

    public ArrayList<String> getPplList() {
        return pplArrayList;
    }

    public void setPplList(String string) {
        // pplListString에 들어있는 id 쪼개서 어레이리스트에 넣기
        // int ppl 과 ArrayList 길이 맞추기
        // 안맞으면 ..? ㅜㅠㅠ
        pplArrayList =  new ArrayList<String>(Arrays.asList(string.split(":")));
        if (pplArrayList.size()!=ppl){
            Log.d(TAG, "setPplList: pplArrayList.size()!=ppl ");
        }
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

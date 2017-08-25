package com.parkhanee.tinychat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.parkhanee.tinychat.classbox.Friend;
import com.parkhanee.tinychat.classbox.Room;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by parkhanee on 2017. 7. 27..
 * Singleton Class
 */

public final class MyPreferences {
    private static MyPreferences mInstance=null;
    private SharedPreferences pref;
    private static final String PREF_NAME = "pref";
    private static final String LOGIN_KEY = "login";
    private final String TAG = "MyPreferences";
    private Context context;

    private MyPreferences(Context context){
        pref = context.getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        this.context = context.getApplicationContext();
    }

    public static synchronized MyPreferences getInstance(Context context) {
        if (mInstance == null){
            mInstance = new MyPreferences(context);
        }
        return mInstance;
    }

    public void putString(String key, String value) {
        pref.edit()
                .putString(key, value)
                .apply();
    }

    public void putInt(String key, int value){
        pref.edit()
                .putInt(key, value)
                .apply();
    }

    public void putBoolean(String key,Boolean b){
        pref.edit()
                .putBoolean(key,b)
                .apply();
    }

    public String getString(String key) {
        return pref.getString(key,"");
    }

    public String getId(){
        // TODO: 2017. 8. 24. id 없을때 처리 ?
        return pref.getString("id","");
    }

    public int getInt(String key){
        return pref.getInt(key,0);
    }

    public Boolean getBoolean(String key){
        return pref.getBoolean(key,false);
    }


    public void remove(String key) {
        pref.edit()
                .remove(key)
                .apply();
    }

    public boolean logout() {
        SharedPreferences.Editor editor = pref.edit();
        Boolean b = editor.clear().commit();
        if (b){
            editor.putBoolean(LOGIN_KEY,false).apply();
        }
        return b;
    }

    public void login(String id, String nid, String name, String img, String created){
        pref.edit()
                .putBoolean(LOGIN_KEY,true)
                .apply();

        // 유저 정보 저장
        putString("id",id);
        putString("nid",nid);
        putString("name",name);
        putString("img",img);
        putString("created",created);
    }
    public void login(String id, String nid, String name, String img,String img_blob, String created){
        pref.edit()
                .putBoolean(LOGIN_KEY,true)
                .apply();

        // 유저 정보 저장
        putString("id",id);
        putString("nid",nid);
        putString("name",name);
        putString("img",img);
        putString("img_blob",img_blob);
        putString("created",created);
    }

    public void setThumb(String thumb){
        putString("img_blob",thumb);
        Log.d(TAG, "setThumb: "+thumb);
    }


    public boolean clear() {
        return pref.edit()
                .clear()
                .commit();
    }

    public boolean contains(String s){
        return pref.contains(s);
    }

    public boolean ifLoggedIn(){
        return pref.getBoolean(LOGIN_KEY,false);
    }

    /**
     *  @param rid 방 아이디
     *  @return rid에 해당하는 방에 들어있는 참가자를 pref 에서 찾아서 Room 객체를 리턴
     * */
    public Room getRoomFromId(String rid){
        Room room;

        //ppl345 -  2:68620823,11111111
        String pplString = getString("ppl"+rid); // pplString = "2:68620823,11111111"
        String[] pplStrings = pplString.split(":"); // pplStrings = [ "2" , "68620823,11111111" ]

        if (pplString.equals("")){ // pref에 해당 방 정보가 없는 경우
            return null;
        }

        room = new Room(rid,Integer.parseInt(pplStrings[0]),pplStrings[1],context);
        return room;
    }

    public boolean isRoomSet(String rid){
        String string = getString("rooms");
        if (string.equals("")){ // 방이 아무것도 없는 경우
            return false;
        } else { // 방 목록을 가져와서 하나씩 검사
            String[] rooms = (string.split(":"))[1].split(",");
            for (String room : rooms){
                if (room.equals(rid)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addRoom(Room room){

        // "rid" 에다가 123 하나 추가, 방 개수 하나 늘리기
        String rid = room.getRid();
        if (!isRoomSet(rid)){
            String string = getString("rooms");
            String[] strings = string.split(":");
            String count = String.valueOf(Integer.parseInt(strings[0]) + 1) ; // 방 개수 하나 늘림
            String rooms = strings[1] + ","+rid;

            putString("rooms",count+":"+rooms); // 새로운 rid 정보 저장

        } else { // 방이 이미 추가되어 있음.
            return false;
        }

        // "ppl123"에 참여자 목록 넣기
        if (room.isPrivate()){
            String participant = room.getParticipant().getId();
            putString("ppl"+rid,"1:"+participant);
        } else {
            String count = String.valueOf(room.getPpl());
            String ppl = "";
            int i=0;
            for (Friend friend : room.getParticipants()){
                if (i!=0){
                    ppl += ",";
                }
                ppl += friend.getId();
                i++;
            }
            putString("ppl"+rid,count+":"+ppl);
        }

        return true;

    }
}

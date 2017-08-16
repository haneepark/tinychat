package com.parkhanee.tinychat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


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

    private MyPreferences(Context context){
        pref = context.getSharedPreferences(PREF_NAME,MODE_PRIVATE);
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
}

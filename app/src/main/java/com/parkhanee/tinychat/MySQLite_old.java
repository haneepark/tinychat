package com.parkhanee.tinychat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by parkhanee on 2017. 8. 1..
 */

public class MySQLite_old extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDB.db";
    public static final String FRIEND_TABLE_NAME = "friend";
    public static final String FRIEND_COLUMN_ID = "id";
    public static final String FRIEND_COLUMN_NID = "nid";
    public static final String FRIEND_COLUMN_NAME = "name";
    public static final String FRIEND_COLUMN_IMG = "img";
    public static final String FRIEND_COLUMN_CREATED = "created";

    public MySQLite_old(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO: 2017. 8. 1. add img column
        db.execSQL(
                "create table friend " +
                        "( id integer primary key, nid integer, name text, created integer )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS friend");
        onCreate(db);
    }

    public boolean insertFriend (int id, int nid, String name, int created) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FRIEND_COLUMN_ID, id);
        contentValues.put(FRIEND_COLUMN_NID, nid);
        contentValues.put(FRIEND_COLUMN_NAME, name);
//        contentValues.put(FRIEND_COLUMN_IMG, img);
        contentValues.put(FRIEND_COLUMN_CREATED, created);
        db.insert("contacts", null, contentValues);
        return true;
    }

    public Cursor getFriend(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from friend where id="+id+"", null );
        return res;
        // TODO: 2017. 8. 1. return array instead of Cursor
    }

    public int numberOfRows(){
        // TODO: 2017. 8. 1. 어느 table 말하는건지 입력받던가, 모든 테이블의 row수 찾아서 다 리턴하던가
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FRIEND_TABLE_NAME);
        return numRows;
    }

    public boolean updateFriend (int id, int nid, String name, int created) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FRIEND_COLUMN_ID, id);
        contentValues.put(FRIEND_COLUMN_NID, nid);
        contentValues.put(FRIEND_COLUMN_NAME, name);
//        contentValues.put(FRIEND_COLUMN_IMG, img);
        contentValues.put(FRIEND_COLUMN_CREATED, created);
        db.update(FRIEND_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteFriend (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FRIEND_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }


    // TODO: 2017. 8. 1.  fetch friend list!
    public ArrayList<String> getAllFriend() {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+FRIEND_TABLE_NAME, null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            // 이름만 인덱스에 넣는건가 ?
            array_list.add(res.getString(res.getColumnIndex(FRIEND_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }
}

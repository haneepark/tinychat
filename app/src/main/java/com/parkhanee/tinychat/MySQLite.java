package com.parkhanee.tinychat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.parkhanee.tinychat.classbox.Friend;
import com.parkhanee.tinychat.classbox.Room;

import java.util.ArrayList;

/**
 * Created by parkhanee on 2017. 8. 2..
 * https://stackoverflow.com/a/3684855/6653855
 * 위의 예시를 MyVolley 클래스 참고해서 싱글톤으로 고침.
 */

public final class MySQLite {
    private static MySQLite mySQLite=null;
    private static final String TAG = "MySQLite";
    private Context applicationContext; // use Application Context not Activity Context
    private SQLiteDatabase mySQLiteDatabase;
    private static MySQLiteHelper mySQLiteHelper; // TODO: 2017. 8. 2. static?

    // 맨 처음 한번만 호출되는 생성자.
    // MySQLite 객체와 아래 세 개의 객체는 모두 맨 처음 한번만 생성되어 싱글톤으로 사용 됨.
    private MySQLite(Context context){
        applicationContext = context.getApplicationContext();
        mySQLiteHelper = getMySQLiteHelper();
        // TODO: 2017. 8. 2. getWritableDatabase must be called in Background Thread !!?
        mySQLiteDatabase = getMySQLiteDatabase();
    }

    // 외부에서 MySQLite 객체가 필요할 때 마다, 즉 db가 필요할 때 마다 호출
    public static synchronized MySQLite getInstance(Context context){
        if (mySQLite == null){
            mySQLite = new MySQLite(context);
        }
        return mySQLite;
    }

    // MySQLite 생성자에서 호출.
    private MySQLiteHelper getMySQLiteHelper(){
        if (mySQLiteHelper == null){
            mySQLiteHelper = new MySQLiteHelper(applicationContext);
        }
        return mySQLiteHelper;
    }

    private boolean isOpen() {
        return mySQLiteDatabase != null && mySQLiteDatabase.isOpen();
    }

    // MySQLite 생성자에서 호출
    public SQLiteDatabase getMySQLiteDatabase(){
        if (!isOpen()){
            mySQLiteDatabase = mySQLiteHelper.getWritableDatabase();
        }
        return mySQLiteDatabase;
    }

    public void close() {
        if (isOpen()) {
            mySQLiteDatabase.close();
            mySQLiteDatabase = null;
            if (mySQLiteHelper != null) {
                mySQLiteHelper.close();
                mySQLiteHelper = null;
            }
        }
    }



    /**
    * friend table 의 상수들 정의.
    * 테이블 이름과 컬럼들의 이름을 정의한다.
    */
    public static final class FriendTable implements BaseColumns {
        // TODO: 2017. 8. 2. 이거 .. 클래스말고 ENUM인가 그걸로 처리할수도 있을 것 같은데
        public static final String TABLE_NAME = "friend";
        public static final String ID = "id";
        public static final String NID = "nid";
        public static final String NAME = "name";
        public static final String IMG = "img";
        public static final String CREATED = "created";
    }

    public boolean addFriend (Friend friend) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FriendTable.ID, friend.getId());
        contentValues.put(FriendTable.NID, friend.getNid());
        contentValues.put(FriendTable.NAME, friend.getName());
        contentValues.put(FriendTable.IMG, friend.getImg());
        contentValues.put(FriendTable.CREATED, friend.getCreated());
        mySQLiteDatabase.insert(FriendTable.TABLE_NAME, null, contentValues);
        return true;
    }

    public Friend getFriend(String id) {
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ FriendTable.TABLE_NAME+" where "+ FriendTable.ID+"="+id+";", null );
        if (cursor != null)
            cursor.moveToFirst();

        Friend friend = new Friend(
                cursor.getString(0), //id
                cursor.getString(1), //nid
                cursor.getString(2), //name
                cursor.getString(3), //img
                cursor.getInt(4) // created
        );
        Log.d(TAG, "getFriend: "+friend.toString());
        return friend;
    }

    public boolean updateFriend (Friend friend) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FriendTable.ID, friend.getId());
        contentValues.put(FriendTable.NID, friend.getNid());
        contentValues.put(FriendTable.NAME, friend.getName());
        contentValues.put(FriendTable.IMG, friend.getImg());
        contentValues.put(FriendTable.CREATED, friend.getCreated());
        mySQLiteDatabase.update(FriendTable.TABLE_NAME, contentValues, "id = ? ", new String[] { friend.getId() } );
        return true;
    }

    // TODO: 2017. 8. 2.  ArrayList OR List ????
    // TODO: 2017. 8. 2. 친구 순서
    public ArrayList<Friend> getAllFriends() {
        ArrayList<Friend> friends = new ArrayList<Friend>();

        // 1. build the query
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ FriendTable.TABLE_NAME, null );

        // 2. go over each row, build friend and add it to arraylist
        if (cursor.moveToFirst()) {
            do {
                Friend friend = new Friend(
                        cursor.getString(0), //id
                        cursor.getString(1), //nid
                        cursor.getString(2), //name
                        cursor.getString(3), //img
                        cursor.getInt(4) // created
                );

                friends.add(friend);
            } while (cursor.moveToNext());
        }
        Log.d(TAG, "getAllFriends: "+friends.toString());
        return friends;
    }

    /**
     * room table 의 상수들 정의.
     * 테이블 이름과 컬럼들의 이름을 정의한다.
     */
    public static final class RoomTable implements BaseColumns {
        // TODO: 2017. 8. 2. 이거 .. 클래스말고 ENUM인가 그걸로 처리할수도 있을 것 같은데
        public static final String TABLE_NAME = "room";
        public static final String RID = "rid";
        public static final String PPL = "ppl";
    }

    public boolean addRoom(Room room){
        ContentValues contentValues = new ContentValues();
        contentValues.put(RoomTable.RID, room.getRid());
        contentValues.put(RoomTable.PPL, room.getPpl());
        mySQLiteDatabase.insert(FriendTable.TABLE_NAME, null, contentValues);
        return true;
    }

    public Room getRoom(String rid) {
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ RoomTable.TABLE_NAME+" where "+ RoomTable.RID+"="+rid+";", null );
        if (cursor != null)
            cursor.moveToFirst();

            Room room = new Room(
                    cursor.getString(0), //rid
                    cursor.getInt(1) // ppl
            );

        Log.d(TAG, "getRoom: "+room.toString());
        return room;
    }

    // TODO: 2017. 8. 2.  ArrayList OR List ????
    // TODO: 2017. 8. 2. 방 순서
    public ArrayList<Room> getAllRooms() {
        ArrayList<Room> rooms = new ArrayList<Room>();

        // 1. build the query
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ RoomTable.TABLE_NAME, null );

        // 2. go over each row, build room and add it to arraylist
        if (cursor.moveToFirst()) {
            do {
                Room room = new Room(
                        cursor.getString(0), //rid
                        cursor.getInt(1) // ppl
                );
                rooms.add(room);
            } while (cursor.moveToNext());
        }
        Log.d(TAG, "getAllRooms: "+rooms.toString());
        return rooms;
    }



    /**
    * 전체 데이터베이스 관리(즉 데이터베이스 생성 및 가져오기, 테이블생성, 버전관리) 하는 클래스
    * */
    private static class MySQLiteHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "db";
        private static final int DATABASE_VERSION = 1;

        private MySQLiteHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            // table 늘어날 때 마다
            createFriendTable(sqLiteDatabase);
            createRoomTable(sqLiteDatabase);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            dropDatabase(sqLiteDatabase);
            onCreate(sqLiteDatabase);
            Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + "!");
        }

        private void dropDatabase(SQLiteDatabase db){
            // table 늘어날 때 마다
            db.execSQL("DROP TABLE IF EXISTS "+ FriendTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS "+ RoomTable.TABLE_NAME);
        }

        private void createFriendTable(SQLiteDatabase db) {
            // TODO: 2017. 8. 2. save Thumbnail image into a new column with blob type
            db.execSQL(
                    "CREATE TABLE " + FriendTable.TABLE_NAME + " ("
//                    + FriendTable._ID + " INTEGER PRIMARY KEY,"
                    + FriendTable.ID + " TEXT PRIMARY KEY,"
                    + FriendTable.NID + " TEXT,"
                    + FriendTable.NAME + " TEXT,"
                    + FriendTable.IMG + " TEXT,"
                    + FriendTable.CREATED + " INTEGER );"
            );

            // sample friend
            db.execSQL("INSERT INTO FRIEND VALUES ( '12341234', '01012341234', '일이삼사', '', 1501659026 )");
            db.execSQL("INSERT INTO FRIEND VALUES ( '11111234', '01011111234', '일일일일', '', 1501659469 )");

        }

        private void createRoomTable(SQLiteDatabase db){
            // TODO: 2017. 8. 2. boolean isPrivateRoom ??
            db.execSQL(
                    "CREATE TABLE " + RoomTable.TABLE_NAME + " ("
                            + RoomTable.RID + " TEXT PRIMARY KEY,"
                            + RoomTable.PPL + " INTEGER );"
            );

            db.execSQL("INSERT INTO ROOM VALUES ( '1', 3 )" );
            db.execSQL("INSERT INTO ROOM VALUES ( '2', 1 )" );
            db.execSQL("INSERT INTO ROOM VALUES ( '3', 1 )" );
            db.execSQL("INSERT INTO ROOM VALUES ( '4', 2 )" );
            db.execSQL("INSERT INTO ROOM VALUES ( '5', 1 )" );
        }
    }

}

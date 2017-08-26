package com.parkhanee.tinychat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parkhanee.tinychat.classbox.Chat;
import com.parkhanee.tinychat.classbox.Friend;

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

    public void logout(){
        // 여기서 drop table만 해버리면 로그아웃 했다가 다시 로그인 할때 테이블이 없어서 에러 남. 없앴다가 미리 다시 만듦.
        mySQLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ FriendTable.TABLE_NAME);
        mySQLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ ChatTable.TABLE_NAME);
        mySQLiteHelper.createFriendTable(mySQLiteDatabase);
        mySQLiteHelper.createChatTable(mySQLiteDatabase);
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
    private static final class FriendTable implements BaseColumns {
        static final String TABLE_NAME = "friend";
        static final String ID = "id";
        static final String NID = "nid";
        static final String NAME = "name";
        static final String RID = "rid";
        static final String IMG_URL = "img_url";
        static final String IMG_BLOB = "img_blob";
        static final String CREATED = "created";

    }

    public boolean addFriend (Friend friend) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FriendTable.ID, friend.getId());
        contentValues.put(FriendTable.NID, friend.getNid());
        contentValues.put(FriendTable.NAME, friend.getName());
        contentValues.put(FriendTable.RID, friend.getRid());
        contentValues.put(FriendTable.IMG_URL, friend.getImgUrl());
        contentValues.put(FriendTable.IMG_BLOB, friend.getImgBlob());
        contentValues.put(FriendTable.CREATED, friend.getCreated());
        mySQLiteDatabase.insert(FriendTable.TABLE_NAME, null, contentValues);
        Log.d(TAG, "addFriend: "+friend.toString());
        return true;
    }

    public Friend getFriend(String id) {
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ FriendTable.TABLE_NAME+" where "+ FriendTable.ID+"="+id+";", null );
        if (cursor != null)
            cursor.moveToFirst();

        try {
            Friend friend = new Friend(
                    cursor.getString(0), //id
                    cursor.getString(1), //nid
                    cursor.getString(2), //name
                    cursor.getString(3), //rid
                    cursor.getString(4), //img_url
                    cursor.getBlob(5), //img_blob
                    cursor.getInt(6) // created
            );
            Log.d(TAG, "getFriend: "+friend.toString());
            return friend;
        } catch (CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
    }

    public String getFriendName(String id) {
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ FriendTable.TABLE_NAME+" where "+ FriendTable.ID+"="+id+";", null );
        if (cursor != null)
            cursor.moveToFirst();
        String name = null;  //name
        try {
            name = cursor.getString(2);
        } catch (Exception e) {
            e.printStackTrace();
            name = "알수없는 친구";
        }
        Log.d(TAG, "getFriendName : "+ name);
        return name;
    }

    public String getFriendRid(String id) {
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ FriendTable.TABLE_NAME+" where "+ FriendTable.ID+"="+id+";", null );
        if (cursor != null)
            cursor.moveToFirst();
            String rid = cursor.getString(3);  //name
        Log.d(TAG, "getFriendRid : "+ rid);
        return rid;
    }

    public boolean updateFriend (Friend friend) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FriendTable.ID, friend.getId());
        contentValues.put(FriendTable.NID, friend.getNid());
        contentValues.put(FriendTable.NAME, friend.getName());
        contentValues.put(FriendTable.RID, friend.getRid());
        contentValues.put(FriendTable.IMG_URL, friend.getImgUrl());
        contentValues.put(FriendTable.IMG_BLOB, friend.getImgBlob());
        contentValues.put(FriendTable.CREATED, friend.getCreated());
        mySQLiteDatabase.update(FriendTable.TABLE_NAME, contentValues, "id = ? ", new String[] { friend.getId() } );
        return true;
    }

    public boolean deleteFriend (String id){
        getAllFriends();

        mySQLiteDatabase.delete(FriendTable.TABLE_NAME,
                FriendTable.ID+" = ?",
                new String[] { String.valueOf(id) });
        Log.d(TAG, "deleteFriend: "+String.valueOf(id));
        getAllFriends();

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
                        cursor.getString(3), //rid
                        cursor.getString(4), //img_url
                        cursor.getBlob(5), //img_blob
                        cursor.getInt(6) // created
                );

                friends.add(friend);
            } while (cursor.moveToNext());
        }
        Log.d(TAG, "getAllFriends: "+friends.toString());
        return friends;
    }

    @Nullable
    public Friend getFriendFromRid(String rid){
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ FriendTable.TABLE_NAME+" where "+ FriendTable.RID+"="+rid+";", null );
        Friend friend = null;
        try {
            if (cursor != null)
                cursor.moveToFirst();
                friend = new Friend(
                    cursor.getString(0), //id
                    cursor.getString(1), //nid
                    cursor.getString(2), //name
                    cursor.getString(3), //rid
                    cursor.getString(4), //img_url
                    cursor.getBlob(5), //img_blob
                    cursor.getInt(6) // created
                    );

            Log.d(TAG, "getFriendFromRid : "+ friend.toString());
            return friend;

        } catch (CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
            Log.d(TAG, "getFriendFromRid : is null");
            return null;
        }
    }


    /**
     * room table 의 상수들 정의.
     * 테이블 이름과 컬럼들의 이름을 정의한다.
     */
    /*
    private static final class RoomTable implements BaseColumns {
        static final String TABLE_NAME = "room";
        static final String RID = "rid";
        static final String PPL = "ppl";
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

    */

    private static final class ChatTable implements BaseColumns {
        static final String TABLE_NAME = "chat";
        static final String MID = "mid"; // PK
        static final String RID = "rid";
        static final String ID = "id"; // 보낸사람 id
        static final String BODY = "body";
        static final String UNIXTIME = "unixTime";
    }

    public boolean addChat(Chat chat){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChatTable.MID, chat.getMid());
            contentValues.put(ChatTable.RID, chat.getRid());
            contentValues.put(ChatTable.ID, chat.getFrom());
            contentValues.put(ChatTable.BODY, chat.getBody());
            contentValues.put(ChatTable.UNIXTIME, chat.getUnitTime());
            mySQLiteDatabase.insert(ChatTable.TABLE_NAME, null, contentValues);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    public ArrayList<Chat> getAllChatInARoom(String rid){
        ArrayList<Chat> chatArrayList = new ArrayList<>();

        try {
            // 1. build the query
            Cursor cursor =  mySQLiteDatabase.rawQuery(
                    "select * from "+ ChatTable.TABLE_NAME +
                    " where "+ ChatTable.RID+"="+rid +
                    " order by "+ChatTable.UNIXTIME+ " ;"
                    , null ); // 최신역순으로 출력

            // 2. go over each row, build room and add it to arraylist
            if (cursor.moveToFirst()) {
                do {
                    Chat chat = new Chat(
                            cursor.getString(0), //mid
                            cursor.getString(1), //rid
                            cursor.getString(2), //from
                            cursor.getString(3), //body
                            cursor.getString(4) //unixtime
                    );
                    chatArrayList.add(chat);
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "getAllChatInARoom: "+chatArrayList.toString());

            cursor.close();
            return chatArrayList;

        } catch (CursorIndexOutOfBoundsException e) {
            Log.d(TAG, "getAllChatInARoom: CursorIndexOutOfBoundsException");
            e.printStackTrace();
            return null;
        }
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
            createChatTable(sqLiteDatabase);
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
            db.execSQL("DROP TABLE IF EXISTS "+ ChatTable.TABLE_NAME);
        }

        private void createFriendTable(SQLiteDatabase db) {
            // TODO: 2017. 8. 2. save Thumbnail image into a new column with blob type
            db.execSQL(
                    "CREATE TABLE " + FriendTable.TABLE_NAME + " ("
//                    + FriendTable._ID + " INTEGER PRIMARY KEY,"
                    + FriendTable.ID + " TEXT PRIMARY KEY,"
                    + FriendTable.NID + " TEXT,"
                    + FriendTable.NAME + " TEXT,"
                    + FriendTable.RID + " TEXT UNIQUE,"
                    + FriendTable.IMG_URL + " TEXT,"
                    + FriendTable.IMG_BLOB + " BLOB,"
                    + FriendTable.CREATED + " INTEGER );"
            );

//            // sample friend
//            db.execSQL("INSERT INTO FRIEND VALUES ( '91433734', '01091433734', '규백','1', '','', 1501659026 )");
//            db.execSQL("INSERT INTO FRIEND VALUES ( '11111111', '01011111111', '일일일', '', 1501659469 )");

        }

        private void createChatTable(SQLiteDatabase db) {
            // TODO: 2017. 8. 2. save Thumbnail image into a new column with blob type
            db.execSQL(
                    "CREATE TABLE " + ChatTable.TABLE_NAME + " ("
//                    + FriendTable._ID + " INTEGER PRIMARY KEY,"
                            + ChatTable.MID + " TEXT PRIMARY KEY,"
                            + ChatTable.RID + " TEXT,"
                            + ChatTable.ID + " TEXT,"
                            + ChatTable.BODY + " TEXT,"
                            + ChatTable.UNIXTIME + " TEXT );"
            );
//
//            db.execSQL("INSERT INTO "+ChatTable.TABLE_NAME+" VALUES ( '1', '1', '11111111','안녕안녕 메세지', '1501659026' )");
//            db.execSQL("INSERT INTO "+ChatTable.TABLE_NAME+" VALUES ( '2', '1', '11111111','안녕안녕 메세지2222', '1501659048' )");
//            db.execSQL("INSERT INTO "+ChatTable.TABLE_NAME+" VALUES ( '3', '1', '11111111','하핳', '1501659704' )");
        }
    }

}

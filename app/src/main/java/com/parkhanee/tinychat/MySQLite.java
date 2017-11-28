package com.parkhanee.tinychat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import com.parkhanee.tinychat.classbox.Chat;
import com.parkhanee.tinychat.classbox.Friend;
import com.parkhanee.tinychat.classbox.Room;

import java.util.ArrayList;
import java.util.HashMap;

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
    private static MySQLiteHelper mySQLiteHelper;

    // 맨 처음 한번만 호출되는 생성자.
    // MySQLite 객체와 아래 세 개의 객체는 모두 맨 처음 한번만 생성되어 싱글톤으로 사용 됨.
    private MySQLite(Context context){
        applicationContext = context.getApplicationContext();
        mySQLiteHelper = getMySQLiteHelper();
        // TODO: 2017. 8. 2. getWritableDatabase must be called in Background Thread ?
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

    void logout(){
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
    private SQLiteDatabase getMySQLiteDatabase(){
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

    boolean addFriend (Friend friend) {
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
            cursor.close();
            return friend;
        } catch (CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public String getFriendName(String id) {
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ FriendTable.TABLE_NAME+" where "+ FriendTable.ID+"="+id+";", null );
        if (cursor != null)
            cursor.moveToFirst();
        String name = null;  //name
        try {
            assert cursor != null;
            name = cursor.getString(2);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        cursor.close();
        Log.d(TAG, "getFriendName : "+ name);
        return name;
    }

    public String getFriendRid(String id) {
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ FriendTable.TABLE_NAME+" where "+ FriendTable.ID+"="+id+";", null );
        if (cursor != null)
            cursor.moveToFirst();
            String rid = cursor.getString(3);  //name
        Log.d(TAG, "getFriendRid : "+ rid);
        cursor.close();
        return rid;
    }

    boolean updateFriend (Friend friend) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FriendTable.ID, friend.getId());
        contentValues.put(FriendTable.NID, friend.getNid());
        contentValues.put(FriendTable.NAME, friend.getName());
        contentValues.put(FriendTable.RID, friend.getRid());
        contentValues.put(FriendTable.IMG_URL, friend.getImgUrl());
        contentValues.put(FriendTable.IMG_BLOB, friend.getImgBlob());
        contentValues.put(FriendTable.CREATED, friend.getCreated());
        mySQLiteDatabase.update(FriendTable.TABLE_NAME, contentValues, "id = ? ", new String[] { friend.getId() } );
        Log.d(TAG, "updateFriend: "+friend.toString());
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

    // TODO: 2017. 8. 2. 친구 순서
    ArrayList<Friend> getAllFriends() {
        ArrayList<Friend> friends = new ArrayList<>();

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
            cursor.close();
        }
        Log.d(TAG, "getAllFriends: "+friends.toString());
        return friends;
    }

    @Nullable
    Friend getFriendFromRid(String rid){
        Cursor cursor =  mySQLiteDatabase.rawQuery( "select * from "+ FriendTable.TABLE_NAME+" where "+ FriendTable.RID+"="+rid+";", null );
        Friend friend;
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
            cursor.close();
            return friend;

        } catch (CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
            Log.d(TAG, "getFriendFromRid : is null");
            return null;
        } catch (SQLException d){
            d.printStackTrace();
            return null;
        }
    }

    private static final class ChatTable implements BaseColumns {
        static final String TABLE_NAME = "chat";
        static final String MID = "mid"; // PK
        static final String RID = "rid";
        static final String ID = "id"; // 보낸사람 id
        static final String BODY = "body";
        static final String UNIXTIME = "unixTime";
        static final String TYPE = "type"; // 0:전송완료 1:전송중 2:전송실패
        static final int TYPE_COMPLETE = 0;
        static final int TYPE_SENDING = 1;
        static final int TYPE_FAIL = 2;
    }

    boolean addChat(Chat chat){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChatTable.MID, chat.getMid());
            contentValues.put(ChatTable.RID, chat.getRid());
            contentValues.put(ChatTable.ID, chat.getFrom());
            contentValues.put(ChatTable.BODY, chat.getBody());
            contentValues.put(ChatTable.UNIXTIME, chat.getUnitTime());
            contentValues.put(ChatTable.TYPE, ChatTable.TYPE_COMPLETE);
            mySQLiteDatabase.insert(ChatTable.TABLE_NAME, null, contentValues);
            return true;
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 메세지 보낼 때는 addChat 대신에  addSendingChat --> updateChat
     * @param type  0:전송완료 1:전송중 2:전송실패
     * */
    boolean addSendingChat(Chat chat, int type){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChatTable.MID, chat.getMid());
            contentValues.put(ChatTable.RID, chat.getRid());
            contentValues.put(ChatTable.ID, chat.getFrom());
            contentValues.put(ChatTable.BODY, chat.getBody());
            contentValues.put(ChatTable.UNIXTIME, chat.getUnitTime());
            contentValues.put(ChatTable.TYPE, type);
            mySQLiteDatabase.insert(ChatTable.TABLE_NAME, null, contentValues);
            return true;
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 보내는 중인 메세지 다 찾아서 전송 실패로 바꿔주기
     */
    boolean updateSendingChatToFail(){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChatTable.TYPE, ChatTable.TYPE_FAIL);
            mySQLiteDatabase.update(ChatTable.TABLE_NAME, contentValues,ChatTable.TYPE+" = ? ", new String[] {String.valueOf(ChatTable.TYPE_SENDING)} );
            Log.d(TAG, "updateSendingChatToFail: ");
            return true;
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            return false;
        }
    }


    boolean updateChat(Chat chat, int type){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChatTable.MID, chat.getMid());
            contentValues.put(ChatTable.RID, chat.getRid());
            contentValues.put(ChatTable.ID, chat.getFrom());
            contentValues.put(ChatTable.BODY, chat.getBody());
            contentValues.put(ChatTable.UNIXTIME, chat.getUnitTime());
            contentValues.put(ChatTable.TYPE, type);
            mySQLiteDatabase.update(ChatTable.TABLE_NAME, contentValues,ChatTable.MID+" = ? ",new String[] { chat.getMid() } );
            Log.d(TAG, "failSendingChat: "+chat.toString());
            return true;
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 메세지 보낼 때는 addChat 대신에  addSendingChat --> updateChat
     * @param type  0:전송완료 1:전송중 2:전송실패
     * */
    boolean updateChat(String mid, int type){

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChatTable.MID, mid);
            contentValues.put(ChatTable.TYPE, type);
            mySQLiteDatabase.update(ChatTable.TABLE_NAME, contentValues,ChatTable.MID+" = ? ",new String[] { mid } );
            Log.d(TAG, "updateChat: "+mid+" "+type);
            return true;
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 사용자가 전송실패한 메세지 "삭제" 누른 경우
    boolean deleteSendingChat(String mid){
        mySQLiteDatabase.delete(ChatTable.TABLE_NAME,
                ChatTable.MID+" = ?",
                new String[] { String.valueOf(mid) });
        Log.d(TAG, "deleteSendingChat: "+String.valueOf(mid));
        return true;
    }

    @Nullable
    ArrayList<Chat> getAllSendingChatInARoom(String rid){
        ArrayList<Chat> chatArrayList = new ArrayList<>();

        try {
            Cursor cursor =  mySQLiteDatabase.rawQuery(
                    "select * from "+ ChatTable.TABLE_NAME +
                            " where "+ ChatTable.RID+"="+rid +
                            " and "+ChatTable.TYPE+"="+ChatTable.TYPE_SENDING+
                            " order by "+ChatTable.UNIXTIME+ " ;"
                    , null ); // 최신역순으로 출력

            if (cursor.moveToFirst()) {
                do {
                    String mid=cursor.getString(0);
                    String from = cursor.getString(2);
                    String body = cursor.getString(3);
                    String unixTime = cursor.getString(4);
                    int type = cursor.getInt(5);
                    Chat chat = new Chat(mid, rid, from, body, unixTime,type);
                    chatArrayList.add(chat);
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "getAllSendingChatInARoom: chat개수: "+chatArrayList.size());

            cursor.close();
            return chatArrayList;

        } catch (CursorIndexOutOfBoundsException e) {
            Log.d(TAG, "getAllChatInARoom: CursorIndexOutOfBoundsException");
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    ArrayList<Chat> getAllFailedChatInARoom(String rid){
        ArrayList<Chat> chatArrayList = new ArrayList<>();

        try {
            Cursor cursor =  mySQLiteDatabase.rawQuery(
                    "select * from "+ ChatTable.TABLE_NAME +
                            " where "+ ChatTable.RID+"="+rid +
                            " and "+ChatTable.TYPE+"="+ChatTable.TYPE_FAIL+
                            " order by "+ChatTable.UNIXTIME+ " ;"
                    , null ); // 최신역순으로 출력

            if (cursor.moveToFirst()) {
                do {
                    String mid=cursor.getString(0);
                    String from = cursor.getString(2);
                    String body = cursor.getString(3);
                    String unixTime = cursor.getString(4);
                    int type = cursor.getInt(5);
                    Chat chat = new Chat(mid, rid, from, body, unixTime,type);
                    chatArrayList.add(chat);
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "getAllSendingChatInARoom: chat개수: "+chatArrayList.size());

            cursor.close();
            return chatArrayList;

        } catch (CursorIndexOutOfBoundsException e) {
            Log.d(TAG, "getAllChatInARoom: CursorIndexOutOfBoundsException");
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    ArrayList<Chat> getAllChatInARoom(String rid){
        ArrayList<Chat> chatArrayList = new ArrayList<>();

        try {
            Cursor cursor =  mySQLiteDatabase.rawQuery(
                    "select * from "+ ChatTable.TABLE_NAME +
                    " where "+ ChatTable.RID+"="+rid +
                            " and "+ChatTable.TYPE+"="+ChatTable.TYPE_COMPLETE+
                    " order by "+ChatTable.UNIXTIME+ " ;"
                    , null ); // 최신역순으로 출력

            if (cursor.moveToFirst()) {
                String unixTime1=null; // 바로 이전항목의 날짜를(unixTime1) 저장해서, 현재 항목(unixTime)과 비교.
                do {
                    String mid=cursor.getString(0);
                    String from = cursor.getString(2);
                    String body = cursor.getString(3);
                    String unixTime = cursor.getString(4);
                    int type = cursor.getInt(5);
                    if (unixTime1==null){ // 맨 처음 항목 뽑을 때
//                        unixTime1 = unixTime;
                        chatArrayList.add(new Chat(unixTime));
                    } else if(MyUtil.FindDateChangeWithUnixTime(unixTime1,unixTime)){// unixTime1 과 unixTime 사이에 날짜가 바뀌었으면 새로운 Chat 객체 저장
                        chatArrayList.add(new Chat(unixTime));
                    }
                    Chat chat = new Chat(mid, rid, from, body, unixTime,type);
                    chatArrayList.add(chat);
                    unixTime1 = unixTime;
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "getAllChatInARoom: chat개수: "+chatArrayList.size());
            Log.d(TAG, "getAllChatInARoom: "+chatArrayList.toString());

            cursor.close();
            return chatArrayList;

        } catch (CursorIndexOutOfBoundsException e) {
            Log.d(TAG, "getAllChatInARoom: CursorIndexOutOfBoundsException");
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    HashMap<String,Chat> getRecentChatInRooms(ArrayList<Room> rooms){
        HashMap<String,Chat> chatHashMap = new HashMap<>();
        try { // 방마다 max(unixtime) 인 chat 하나씩 select
            for (Room room : rooms){
                String rid=room.getRid();

                    Cursor cursor =  mySQLiteDatabase.rawQuery(
                            "SELECT "+ChatTable.MID+", "+ChatTable.RID+", "+ChatTable.ID+", "+ChatTable.BODY+", MAX(" +ChatTable.UNIXTIME+") AS "+ChatTable.UNIXTIME+", "+ChatTable.TYPE+
                                    " FROM "+ ChatTable.TABLE_NAME +
                                    " WHERE "+ ChatTable.RID+"="+rid +
                                        " and "+ChatTable.TYPE+"="+ChatTable.TYPE_COMPLETE+" ;"
                            , null ); // 최신역순으로 출력

                    if (cursor.moveToFirst()) {
                        Chat chat = new Chat(
                                cursor.getString(0), //mid
                                cursor.getString(1), //rid
                                cursor.getString(2), //from
                                cursor.getString(3), //body
                                cursor.getString(4), //unixtime
                                cursor.getInt(5) // type
                        );
                        chatHashMap.put(rid,chat);
                    }

                    cursor.close();

                Log.d(TAG, "getRecentChatInRooms: count: "+chatHashMap.size());
                Log.d(TAG, "getRecentChatInRooms: "+chatHashMap.toString());
            }
        } catch (CursorIndexOutOfBoundsException e) {
            Log.d(TAG, "getRecentChatInRooms: CursorIndexOutOfBoundsException");
            e.printStackTrace();
            return null;
        } catch (Exception e1){
            e1.printStackTrace();
        }

        return chatHashMap;
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
        }

        private void createChatTable(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE " + ChatTable.TABLE_NAME + " ("
//                    + FriendTable._ID + " INTEGER PRIMARY KEY,"
                            + ChatTable.MID + " TEXT PRIMARY KEY,"
                            + ChatTable.RID + " TEXT,"
                            + ChatTable.ID + " TEXT,"
                            + ChatTable.BODY + " TEXT,"
                            + ChatTable.UNIXTIME + " TEXT,"
                            + ChatTable.TYPE + " INTEGER );"
            );
        }
    }
}

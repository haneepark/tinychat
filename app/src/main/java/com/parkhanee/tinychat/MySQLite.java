package com.parkhanee.tinychat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

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

//    // MySQLite 생성자에서 호출.
//    private MySQLiteHelper getMySQLiteHelper(){
//        if (mySQLiteHelper == null){
//            mySQLiteHelper = new MySQLiteHelper(applicationContext);
//        }
//        return mySQLiteHelper;
//    }

    private boolean isOpen() {
        return mySQLiteDatabase != null && mySQLiteDatabase.isOpen();
    }

    // MySQLite 생성자에서 호출
    public SQLiteDatabase getMySQLiteDatabase(){
        if (!isOpen()){
            if (mySQLiteHelper==null){
                mySQLiteHelper = new MySQLiteHelper(applicationContext);
            }
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
    public static final class Friend implements BaseColumns {
        // TODO: 2017. 8. 2. BaseColumns 는 뭐지 !! 왜여기들어갔지 !!
        // TODO: 2017. 8. 2. 클래스말고 ENUM인가 그걸로 처리할수도 있을 것 같은데
        // TODO: 2017. 8. 2. 근데 친구 클래스가 있으면 친구정보 관리하기 편한데 ....  그건 따로 또 만들어야하나
        public static final String TABLE_NAME = "friend";
        public static final String ID = "id";
        public static final String NID = "nid";
        public static final String NAME = "name";
        // TODO: 2017. 8. 2.  여기에는 큰이미지의 서버url저장하고 썸네일은 따로 blob로 저장해야 하나?
        public static final String IMG = "img";
        public static final String CREATED = "created";
    }

    public boolean addFriend (int id, int nid, String name, String img,int created) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Friend.ID, id);
        contentValues.put(Friend.NID, nid);
        contentValues.put(Friend.NAME, name);
        contentValues.put(Friend.IMG, img);
        contentValues.put(Friend.CREATED, created);
        mySQLiteDatabase.insert(Friend.TABLE_NAME, null, contentValues);
        return true;
    }

    public String getFriend(int id) {
        Cursor res =  mySQLiteDatabase.rawQuery( "select * from "+Friend.TABLE_NAME+" where "+Friend.ID+"="+id+";", null );
        res.moveToFirst();
        String fname = res.getString(res.getColumnIndex(Friend.NAME));
        return fname;
        // TODO: 2017. 8. 1. return array instead of Cursor
    }

    public boolean updateFriend (int id, int nid, String name, String img, int created) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Friend.ID, id);
        contentValues.put(Friend.NID, nid);
        contentValues.put(Friend.NAME, name);
        contentValues.put(Friend.IMG, img);
        contentValues.put(Friend.CREATED, created);
        mySQLiteDatabase.update(Friend.TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    // TODO: 2017. 8. 1.  fetch friend list!
    public ArrayList<String> getAllFriends() {
        ArrayList<String> array_list = new ArrayList<String>();

        Cursor res =  mySQLiteDatabase.rawQuery( "select * from "+Friend.TABLE_NAME, null );
        res.moveToFirst();

        // Cursor into Array
        while(!res.isAfterLast()){
            // FIXME: 2017. 8. 2.  이름만 array에 넣는건가 ?
            array_list.add(res.getString(res.getColumnIndex(Friend.NAME)));
            res.moveToNext();
        }
        return array_list;
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
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            dropDatabase(sqLiteDatabase);
            onCreate(sqLiteDatabase);
            Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + "!");
        }

        private void dropDatabase(SQLiteDatabase db){
            // table 늘어날 때 마다
            db.execSQL("DROP TABLE IF EXISTS "+ Friend.TABLE_NAME);
        }

        private void createFriendTable(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE " + Friend.TABLE_NAME + " ("
//                    + Friend._ID + " INTEGER PRIMARY KEY,"
                    + Friend.ID + " INTEGER PRIMARY KEY,"
                    + Friend.NID + " INTEGER,"
                    + Friend.NAME + " TEXT,"
                    + Friend.IMG + " TEXT,"
                    + Friend.CREATED + " INTEGER );"
            );

            // sample friend
            db.execSQL("INSERT INTO FRIEND VALUES (12341234,01012341234,'일이삼사','',1501659026)");
        }
    }

}

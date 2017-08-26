package com.parkhanee.tinychat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.parkhanee.tinychat.classbox.Room;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.parkhanee.tinychat.MyTCPClient.JSON_INFO;
import static com.parkhanee.tinychat.MyTCPClient.JSON_MSG;

/**
 * Created by parkhanee on 2017. 7. 21..
 * Static/Helper Class (Class with only static fields/methods)
 */

public final class MyUtil {

    private static final String TAG = "MyUtil";

    public static boolean IsNetworkConnected(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static boolean nidFormChecker (String s) {
        if(s.isEmpty()) return false;
        int len = s.length();
        if (len!=11&&len!=10) return false;
        for(int i = 0; i < len; i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    public static void logout(Context context){

        MyPreferences pref = MyPreferences.getInstance(context);
        MySQLite db = MySQLite.getInstance(context);
        pref.logout();
        db.logout();

        // TODO: 2017. 8. 11. pref, db close ?

        // TODO: 2017. 8. 12. 알림
        Toast.makeText(context, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();

        // 로그인 액티비티로 가기
        Intent i = new Intent(context,LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    /**
     * return
     * null (on jsonException)
     * or [ JSON_MSG , rid , id , body , unixTime , mid]
     * or [ JSON_INFO , info ]
     * */
    public static List<String> readJSONObject(String source){

        try {

            List<String> result = new ArrayList<>();

            JSONObject object = new JSONObject(source);
            if (object.has(JSON_MSG)){
                JSONObject msgObject = object.getJSONObject(JSON_MSG);
                result.clear();
                result.add(0, JSON_MSG);
                result.add(1,msgObject.getString("rid"));
                result.add(2,msgObject.getString("id")); // 이 메세지 보낸사람 아이디
                result.add(3,msgObject.getString("body"));
                result.add(4,msgObject.getString("unixTime"));
                result.add(5,msgObject.getString("mid"));
            } else if (object.has(JSON_INFO)){
                result.clear();
                result.add(0, JSON_INFO);
                result.add(1,object.getString("info"));
            } else {
                result.add(source);
            }

            Log.d(TAG, "readJSONObject: "+ result);

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // TODO: 2017. 8. 26. unixTime to normal expression
    public static String UnixTimeToDate(String unixTime){

        Date date=new Date(Long.valueOf(unixTime)*1000);
        Date todayDate = new Date();

        String result;

        SimpleDateFormat year = new SimpleDateFormat("yyyy", Locale.KOREA);

        if (year.format(date).equals(year.format(todayDate))){ // 지금 연도와 비교하여 연도가 같음
            SimpleDateFormat day = new SimpleDateFormat("MM/dd", Locale.KOREA);
            if (day.format(date).equals(day.format(todayDate))){ // 오늘과 비교하여 날짜가 같음
                SimpleDateFormat time = new SimpleDateFormat("HH:mm", Locale.KOREA);
                result = time.format(date);
            } else {
                result = day.format(date);
            }

        } else {
            SimpleDateFormat whole = new SimpleDateFormat("yy/MM/dd", Locale.KOREA);
            result = whole.format(date);
        }
        Log.d(TAG, "UnixTimeToDate: "+unixTime+" --> "+result);

        return result;
    }

    /**
     *  인텐트로 불러온 이미지를 비트맵으로 저장할 때 자동으로 -90도 돌아가는거 조정해주는 클래스
     *  http://stackoverflow.com/a/20634331/6653855
     *
     * @see  // http://sylvana.net/jpegcrop/exif_orientation.html
     */
    public static Bitmap rotateBitmap(String src, Bitmap bitmap) throws InvocationTargetException {
        try {
            int orientation = getExifOrientation(src);

            if (orientation == 1) {
                return bitmap;
            }

            Matrix matrix = new Matrix();
            switch (orientation) {
                case 2:
                    matrix.setScale(-1, 1);
                    break;
                case 3:
                    matrix.setRotate(180);
                    break;
                case 4:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case 5:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case 6:
                    matrix.setRotate(90);
                    break;
                case 7:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case 8:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }

            try {
                //                bitmap.recycle();
                return Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static int getExifOrientation(String src) throws IOException, InvocationTargetException {
        int orientation = 1;

        try {
            /**
             * if your are targeting only api level >= 5 ExifInterface exif =
             * new ExifInterface(src); orientation =
             * exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
             */
            if (Build.VERSION.SDK_INT >= 5) {
                Class<?> exifClass = Class
                        .forName("android.media.ExifInterface");
                Constructor<?> exifConstructor = exifClass
                        .getConstructor(new Class[]{String.class});
                Object exifInstance = exifConstructor
                        .newInstance(new Object[]{src});
                Method getAttributeInt = exifClass.getMethod("getAttributeInt",
                        new Class[]{String.class, int.class});
                Field tagOrientationField = exifClass
                        .getField("TAG_ORIENTATION");
                String tagOrientation = (String) tagOrientationField.get(null);
                orientation = (Integer) getAttributeInt.invoke(exifInstance,
                        new Object[]{tagOrientation, 1});
            }
        } catch (ClassNotFoundException | SecurityException | NoSuchMethodException | InstantiationException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return orientation;
    }
}

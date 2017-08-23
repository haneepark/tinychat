package com.parkhanee.tinychat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.parkhanee.tinychat.classbox.Friend;
import com.parkhanee.tinychat.classbox.Room;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by parkhanee on 2017. 7. 21..
 * Static/Helper Class (Class with only static fields/methods)
 */

public final class MyUtil {

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
     *  @param rid 방 아이디
     *  @return rid에 해당하는 방에 들어있는 참가자를 pref 에서 찾아서 Room 객체를 리턴
    * */
    public static Room initRoom(Context context, MyPreferences pref,String rid){
        Room room;

        //ppl345 -  2:68620823,11111111
        String pplString = pref.getString("ppl"+rid); // pplString = "2:68620823,11111111"
        String[] pplStrings = pplString.split(":"); // pplStrings = [ "2" , "68620823,11111111" ]

        if (pplString.equals("")){ // pref에 해당 방 정보가 없는 경우
            return null;
        }

        room = new Room(rid,Integer.parseInt(pplStrings[0]),pplStrings[1],context);
        return room;
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

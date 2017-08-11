package com.parkhanee.tinychat;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

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
}

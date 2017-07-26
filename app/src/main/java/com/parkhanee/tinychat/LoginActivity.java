package com.parkhanee.tinychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity  implements View.OnClickListener{

    EditText et_nid, et_pwd;
    Button btn_ok;
    TextView tv;
    String appendUrl = "login.php";
    public static final String TAG = "Login";
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: 2017. 7. 24.  check if !logged in ? finish() : setContentView
        // login check with shared preference

        setContentView(R.layout.activity_login);

        et_nid = (EditText) findViewById(R.id.nid);
        et_nid.requestFocus();
        et_pwd = (EditText) findViewById(R.id.pwd);
        btn_ok = (Button) findViewById(R.id.button);
        btn_ok.setOnClickListener(this);
        tv = (TextView) findViewById(R.id.textView6);
        tv.setOnClickListener(this);
    }

    public void onRequested (final String nid, final String pwd) {

        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
                getRequestQueue();

        String url = getString(R.string.server)+appendUrl;

        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String t = "Response: " + response;
                tv.setText(t);
                Log.d(TAG, t);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tv.setText("That didn't work!");
                Log.d(TAG, "onErrorResponse: "+error.getMessage());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("nid",nid);
                params.put("pwd",pwd);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded"); //form ?
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    // TODO: 2017. 7. 24. onCancelButtonClicked --> queue.cancelAll(LoginTag)
    // 사용자가 취소버튼 눌렀을 때 디폴트동작은 뭐지? 여기서 이 요청을 캔슬 해주는게 필요한가 ???
    // 종료하시겠습니까 ? <-- 어디서 나오게 할지 ? 이게 로그인activity, 메인 activiy에서 할수있는 액션이니까 singleton 구현 ?


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.textView6 : // 회원가입
                Intent i = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.button : // 로그인
                String nid = et_nid.getText().toString();
                String pwd = et_pwd.getText().toString();
                if (IsReadyToLogin(nid,pwd)) {
                    // make http request
                    onRequested(nid,pwd);
                }
                break;
        }
    }

    /*
    * LoginHandler
    * 입력받은 아이디와 비밀번호의 형식을 확인하고
    * 인터넷 연결 확인하고
    * 서버에 로그인 시도
    * */
    public boolean IsReadyToLogin (String nid, String pwd) {
        if (!Util.IsNetworkConnected(this)) {
            // TODO: 2017. 7. 21. network 연결안됨 경고
            Toast.makeText(this, "인터넷 연결 안됨", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!nidFormChecker(nid)) {
            // TODO: 2017. 7. 21. 경고 ?
            Toast.makeText(this, "아이디 형식 틀림", Toast.LENGTH_SHORT).show();
            return false;
        } else if (pwd.length()<=0){
            // TODO: 2017. 7. 21. 경고
            Toast.makeText(this, "비밀번호 입력 안함", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
}

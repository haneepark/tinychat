package com.parkhanee.tinychat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity  implements View.OnClickListener{

    EditText et_nid, et_pwd;
    String appendUrl = "login.php";
    public static final String TAG = "LoginActivity";
    RequestQueue queue;
    MyPreferences pref=null;
    Context context=this; // Activity context
    String nid,pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (pref==null){
            pref = MyPreferences.getInstance(context);
        }
        if (pref.ifLoggedIn()){
            Intent i = new Intent(context,MainActivity.class);
            startActivity(i);
            finish();
        }

        // 로그인 확인하고 나서 setContentView
        setContentView(R.layout.activity_login);

        et_nid = (EditText) findViewById(R.id.nid);
        et_nid.requestFocus();
        et_pwd = (EditText) findViewById(R.id.pwd);

        (findViewById(R.id.button)).setOnClickListener(this);
        (findViewById(R.id.textView6)).setOnClickListener(this);

    }

    public void onLoginRequested () {

        queue = MyVolley.getInstance(this.getApplicationContext()).
                getRequestQueue();

        String url = getString(R.string.server)+appendUrl;

        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    String result = jsonObject.getString("result");
                    // result code 확인
                    if (!resultCode.equals("100")){
                        // TODO: 2017. 7. 29. 로그인 실패 처리 경고
                        Toast.makeText(LoginActivity.this,"로그인 실패, code : " + resultCode+", result : "+result, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // TODO: 2017. 7. 26. 로그인에 성공 했습니다 알림
                    Toast.makeText(LoginActivity.this, "로그인에 성공 했습니다", Toast.LENGTH_SHORT).show();
                    // sharedPreference에 로그인 성공 저장
                    pref.login();

//                    String name = jsonObject.getString("name");
//                    String id = jsonObject.getString("id");
//                    String created = jsonObject.getString("created");
//
//                    String jsonString = "";
//                    jsonString += "name: " + name + "\n\n";
//                    jsonString += "id: " + id + "\n\n";
//                    jsonString += "created: " + created + "\n\n";
//
//                    Log.d(TAG, "onResponse:"+jsonString);


                    // 유저 정보 SP에 저장
                    pref.putString("id",jsonObject.getString("id"));
                    pref.putString("nid",jsonObject.getString("nid"));
                    pref.putString("name",jsonObject.getString("name"));
                    pref.putString("img",jsonObject.getString("img"));
                    pref.putString("created",jsonObject.getString("created"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent i = new Intent(context,MainActivity.class);
                startActivity(i);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: 2017. 7. 26. 로그인에 실패했습니다 아이디와 비밀번호를 확인해주세요 경고
                Toast.makeText(LoginActivity.this, "로그인에 실패 했습니다 서버 에러?", Toast.LENGTH_SHORT).show();
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
    // 종료하시겠습니까 ? <-- 어디서 나오게 할지 ? 이게 로그인activity, 메인 activiy 에서 할 수 있는 액션이니까 singleton 구현 ?


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.textView6 : // 회원가입
                Intent i = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(i);
                // 회원가입 하고나서 로그인 액티비티로 다시 돌아올 거니까 finish() 안함
                break;
            case R.id.button : // 로그인
                nid = et_nid.getText().toString();
                pwd = et_pwd.getText().toString();
                if (IsReadyToLogin()) {
                    // make http request
                    onLoginRequested();
                }
                break;
        }
    }

    /*
    * LoginHandler
    * 인터넷 연결 확인하고
    * 입력받은 아이디와 비밀번호의 형식을 확인하고
    * 불리언 결과 리턴
    * */
    public boolean IsReadyToLogin () {
        if (!MyUtil.IsNetworkConnected(this)) {
            // TODO: 2017. 7. 21. network 연결안됨 경고
            // TODO: 2017. 8. 1. 근데 이거는 백그라운드에서 따로 확인해서 로그인 누르는 것과 관계없이, 그 전에도 경고 나와야 하지 않나
            Toast.makeText(this, "인터넷 연결 안됨", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!MyUtil.nidFormChecker(nid)) {
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

}

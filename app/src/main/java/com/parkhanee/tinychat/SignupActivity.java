package com.parkhanee.tinychat;

import android.content.Context;
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

public class SignupActivity extends AppCompatActivity  implements View.OnClickListener {

    Context context = this;
    public static final String TAG = "SignupActivity";
    RequestQueue queue;
    EditText et_nid,et_pwd, et_pwd2, et_name;
    String nid, name, pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        et_nid = (EditText) findViewById(R.id.nid2);
        et_name = (EditText) findViewById(R.id.name);
        et_pwd = (EditText) findViewById(R.id.pwd2);
        et_pwd2 = (EditText) findViewById(R.id.confirmpwd);

        (findViewById(R.id.signup_btn)).setOnClickListener(this);
        (findViewById(R.id.textView9)).setOnClickListener(this);

    }

    public void onSignupRequested (){
        queue = MyVolley.getInstance(this.getApplicationContext()).
                getRequestQueue();
        String url = getString(R.string.server_url)+getString(R.string.server_signup);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    // result code 확인
                    if (!resultCode.equals("100")){
                        // TODO: 2017. 7. 29. 회원가입 실패 처리 경고
                        Toast.makeText(context,"회원가입 실패, code : " + resultCode, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // TODO: 2017. 7. 26. 로그인에 성공 했습니다 알림
                    Toast.makeText(context, "회원가입에 성공 했습니다. 로그인 해주세요", Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 로그인 액티비티로 돌아가기
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: 2017. 7. 26. 경고
                Toast.makeText(context, "회원가입에 실패 했습니다 서버 에러?", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onErrorResponse: "+error.getMessage());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("nid",nid);
                params.put("name",name);
                params.put("pwd",pwd);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setTag(TAG);
        queue.add(stringRequest);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signup_btn : // create account 버튼
                nid = et_nid.getText().toString();
                name = et_name.getText().toString();
                pwd = et_pwd.getText().toString();
                String pwd2 = et_pwd2.getText().toString();
                if (IsReadyToSignup(pwd2)) {
                    // make http request
                    onSignupRequested();
                }
                break;
            case R.id.textView9 : // 로그인 액티비티로 돌아가기 버튼
                Toast.makeText(context, "go back to login", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    /*
    * signup handler
    * 인터넷 연결 확인하고
    * 입력받은 아이디, 이름, 비밀번호의 형식을 확인하고
    * 불리언 결과 리턴
    * */
    public boolean IsReadyToSignup (String pwd2) {
        if (!MyUtil.IsNetworkConnected(this)) {
            // TODO: 2017. 7. 21. 경고
            // TODO: 2017. 8. 1. 근데 이거는 백그라운드에서 따로 확인해서 로그인 누르는 것과 관계없이, 그 전에도 경고 나와야 하지 않나
            Toast.makeText(this, "인터넷 연결 안됨", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!MyUtil.nidFormChecker(nid)) {
            // TODO: 2017. 7. 21. 경고 ?
            Toast.makeText(this, "아이디 형식 틀림", Toast.LENGTH_SHORT).show();
            return false;
        } else if (name.length()<=0){
            // TODO: 2017. 7. 21. 경고
            Toast.makeText(this, "이름 입력 안함", Toast.LENGTH_SHORT).show();
            return false;
        }   else if (pwd.length()<=0){
            // TODO: 2017. 7. 21. 경고
            Toast.makeText(this, "비밀번호 입력 안함", Toast.LENGTH_SHORT).show();
            return false;
        }  else if (pwd2.length()<=0){
            // TODO: 2017. 7. 21. 경고
            Toast.makeText(this, "비밀번호 확인 입력 안함", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!pwd.equals(pwd2)){
            // TODO: 2017. 8. 1. 경고
            Toast.makeText(context, "비밀번호 두 개가 다름", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    // TODO: 2017. 7. 24. onCancelButtonClicked --> queue.cancelAll(LoginTag)
    // 사용자가 취소버튼 눌렀을 때 디폴트동작은 뭐지? 여기서 이 요청을 캔슬 해주는게 필요한가 ???
}

package com.ulensapp.ulensapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    public static final String TokenUrl = "https://www.ulensapp.com/Token";
    EditText edtUsername, edtPassword;
    String strUsername, strPassword;
    Button btnLogin;
    ProgressBar pbLogin;
    String token;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setStatusBarColor(R.color.ulensPrimary);
        connectToUI();

        edtUsername.setText("okan@ulensapp.com");
        edtPassword.setText("123456");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strUsername = edtUsername.getText().toString();
                strPassword = edtPassword.getText().toString();
                if (canLogin()){
                    loginRequest();
                }


            }
        });

    }

    public void connectToUI(){
        btnLogin = findViewById(R.id.btnLogin);
        edtUsername = findViewById(R.id.edtUsernameLgn);
        edtPassword = findViewById(R.id.edtPasswordLgn);
        pbLogin = findViewById(R.id.pbLogin);
    }

    private void loginRequest() {
        pbLogin.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, TokenUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pbLogin.setVisibility(View.INVISIBLE);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            token = jsonResponse.getString("access_token");
                            saveTokenToPhone();
                            Intent intent = new Intent(getApplicationContext(),HomePage.class);
                            setToRememberMe();
                            startActivity(intent);
                            LoginActivity.this.finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;

                if (networkResponse.statusCode == 500){
                    Toast.makeText(getApplicationContext(),"Server Hatası ! Destek ekibi ile iletişime geçiniz.",Toast.LENGTH_LONG).show();
                    pbLogin.setVisibility(View.INVISIBLE);
                    return;
                }

                Toast.makeText(getApplicationContext(),"Kullanıcı adı veya Şifre hatalı",Toast.LENGTH_LONG).show();
                pbLogin.setVisibility(View.INVISIBLE);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return super.getHeaders();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("grant_type","password");
                params.put("username",edtUsername.getText().toString());
                params.put("password",edtPassword.getText().toString());

                return params;
            }
        };

        queue.add(stringRequest);


    }

    public boolean canLogin (){
        int lengthofUsername = edtUsername.getText().toString().length();
        int lengthOfPassword = edtPassword.getText().toString().length();

        if (lengthofUsername == 0 || lengthOfPassword == 0 ){
            if (lengthofUsername == 0)
                edtUsername.setError("Kullanıcı adı boş bırakılamaz.");
            if (lengthOfPassword == 0)
                edtPassword.setError("Şifre alanı boş bırakılamaz.");

            return false;
        }

        return true;
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int color){
        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,color));
    }

    public void saveTokenToPhone(){
        sharedPref= getSharedPreferences("token", Context.MODE_PRIVATE);
        editor=sharedPref.edit();
        editor.putString("token", token);
        editor.putString("username",edtUsername.getText().toString());
        editor.putString("password",edtPassword.getText().toString());
        editor.commit();
    }

    public void setToRememberMe(){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isRemembered", true);
        editor.putString("username",strUsername);
        editor.putString("password",strPassword);
        editor.commit();
    }



}

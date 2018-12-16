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

public class SplashScreen extends AppCompatActivity {

    String username;
    String password;
    String token;

    ProgressBar pbLogin;
    public static final String TokenUrl = "https://www.ulensapp.com/Token";

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        pbLogin = findViewById(R.id.pbSplash);

        if (!isRemembered()){
            setSplashScreen(1000);
        }else {
            getDetails();
            pbLogin.setVisibility(View.VISIBLE);
            loginRequest();
        }
        setStatusBarColor(R.color.ulensPrimary);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int color){
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,color));
    }

    public void setSplashScreen(int SPLASH_DISPLAY_LENGTH){
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    public boolean isRemembered () {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemembered = settings.getBoolean("isRemembered", false);
        return isRemembered;
    }

    public void getDetails () {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        username = settings.getString("username","0");
        password = settings.getString("password","0");
        Log.e("details", username + " " + password);
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
                            startActivity(intent);
                            SplashScreen.this.finish();

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

                if (networkResponse.statusCode == 400 ) {
                    Toast.makeText(getApplicationContext(),"Bad Request", Toast.LENGTH_LONG).show();
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
                params.put("username",username);
                params.put("password",password);

                return params;
            }
        };

        queue.add(stringRequest);


    }

    public void saveTokenToPhone(){
        sharedPref= getSharedPreferences("token", Context.MODE_PRIVATE);
        editor=sharedPref.edit();
        editor.putString("token", token);
        editor.putString("username",username);
        editor.putString("password",password);
        editor.commit();
    }
}

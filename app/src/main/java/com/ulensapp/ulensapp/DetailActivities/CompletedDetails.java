package com.ulensapp.ulensapp.DetailActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ulensapp.ulensapp.HomePage;
import com.ulensapp.ulensapp.LoginActivity;
import com.ulensapp.ulensapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CompletedDetails extends AppCompatActivity {

    String expenseId, token;
    Button btnBack;

    String detailUrl = "https://www.ulensapp.com/Api/Bill/GetInvoice?invoiceId=";

    String expAmount, expMerchantName, expDate, expUsername, expDescription, expCategoryName;
    TextView txtAmount, txtMerchantName, txtDate, txtUsername, txtDescription, txtCategoryName;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_details);
        setStatusBarColor(R.color.ulensStatusBarColor);
        setDetailStrings();
        setToken();
        getExpenseId();
        connectUI();
        getExpenseDetails();

    }

    public void setDetailStrings() {
        expAmount = "";
        expMerchantName = "";
        expDate = "";
        expUsername = "";
        expDescription = "";
        expCategoryName = "";
    }

    private void connectUI() {
        btnBack = findViewById(R.id.btnBackCompleted);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        sharedPref= getSharedPreferences("token", Context.MODE_PRIVATE);
        editor=sharedPref.edit();
        expUsername = sharedPref.getString("username","000");
        Log.e("username", expUsername);

        txtUsername = findViewById(R.id.txtComDetail);
        txtUsername.setText(expUsername);

        txtAmount = findViewById(R.id.txtAmComDetail2);
        txtMerchantName = findViewById(R.id.txtComMerchant);
        txtDate = findViewById(R.id.txtComDate);
        txtDescription = findViewById(R.id.txtComDescr);
        txtCategoryName = findViewById(R.id.txtComCategoryName);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int color){
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,color));
    }
    public void getExpenseId() {
        expenseId = getIntent().getStringExtra("expenseId");
        detailUrl = detailUrl + expenseId;
        Log.e("expenseId",expenseId);
    }

    public void setToken(){
        sharedPref= getSharedPreferences("token", Context.MODE_PRIVATE);
        editor=sharedPref.edit();
        token = sharedPref.getString("token","000");
    }

    public void getExpenseDetails() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, detailUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            expAmount = jsonResponse.getString("Amount");
                            txtAmount.setText(expAmount);

                            expDate = jsonResponse.getString("Date");
                            txtDate.setText(setToDate(expDate));

                            expMerchantName = jsonResponse.getString("Merchant");
                            txtMerchantName.setText(expMerchantName);

                            expDescription = jsonResponse.getString("Description");
                            txtDescription.setText(expDescription);

                            expCategoryName = jsonResponse.getString("CategoryName");
                            txtCategoryName.setText(expCategoryName);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization","Bearer "+token);

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };

        queue.add(stringRequest);
    }

    public static String setToDate (String date) {
        String lastDate = "Tarih bilgisi bulunamadı.";
        Log.e("date",date);
        String [] parts = date.split("T");
        if (parts.length == 1) {
            return lastDate;
        }
        lastDate = parts[0];
        lastDate = lastDate + " " + parts[1];
        return lastDate;
    }
}

package com.ulensapp.ulensapp.DetailActivities;

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

import com.ulensapp.ulensapp.R;

import static com.ulensapp.ulensapp.R.color.ulensStatusBarColor;

public class PendingDetail extends AppCompatActivity implements View.OnClickListener {

    Button btnBack;
    String expenseId;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_detail);
        getExpenseId();
        setStatusBarColor(ulensStatusBarColor);
        connectUI();
    }

    public void getExpenseId() {
        expenseId = getIntent().getStringExtra("expenseId");
        Log.e("expenseId",expenseId);
    }

    public void connectUI() {
        btnBack = findViewById(R.id.btnBackPending);
        btnBack.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int color){
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,color));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnBackPending){
            onBackPressed();
        }
    }
}

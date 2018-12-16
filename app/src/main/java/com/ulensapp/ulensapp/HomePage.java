package com.ulensapp.ulensapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.yavski.fabspeeddial.FabSpeedDial;

public class HomePage extends AppCompatActivity {

    public static final String pendingURL = "https://www.ulensapp.com/api/Bill/GetInvoices?isConfirmed=0";
    public static final String acceptedURL = "https://www.ulensapp.com/api/Bill/GetInvoices?isConfirmed=1";
    public static final String deniedURL = "https://www.ulensapp.com/api/Bill/GetInvoices?isConfirmed=2";

    final List<Information> pendingDatas = new ArrayList<>();
    final List<Information> completedDatas = new ArrayList<>();

    private RecyclerView rwPending;
    private RecyclerView rwCompleted;
    private PendingAdapter adapter;
    private CompletedAdapter adapter2;
    private DrawerLayout mDrawerLayout;
    private ScrollView scHomepage;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    String token, strUsername;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Toolbar tlbarHomepage;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        setToken();
        setStatusBarColor(R.color.ulensStatusBarColor);
        connectToUI();

        getPendingExpenses();
        getAcceptedExpenses();

        scHomepage = findViewById(R.id.scHomePage);
        scHomepage.post(new Runnable() {
            @Override
            public void run() {
                scHomepage.fullScroll(View.FOCUS_UP);
            }
        });

        rwPending = findViewById(R.id.rwPending);
        rwCompleted = findViewById(R.id.rwCompleted);
       // rwCompleted.setAdapter(adapter);
       // rwCompleted.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.txtUsernameHeader);
        TextView txtLogout = headerView.findViewById(R.id.txtLogout);
        txtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setForget();
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                HomePage.this.finish();
            }
        });
        Switch swNotifs = headerView.findViewById(R.id.swNotifs);
        swNotifs.setEnabled(false);
        swNotifs.setChecked(true);

        checkAndRequestPermissions();

        sharedPref= getSharedPreferences("token", Context.MODE_PRIVATE);
        editor=sharedPref.edit();
        strUsername = sharedPref.getString("username","000");

        navUsername.setText(strUsername);

        FabSpeedDial fabSpeedDial = findViewById(R.id.fabMenu);
        fabSpeedDial.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.capture){
                    Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
                    intent.putExtra("token",token);
                    startActivity(intent);
                }

                return false;
            }

            @Override
            public void onMenuClosed() {

            }
        });

    }

    public void connectToUI(){
        tlbarHomepage = findViewById(R.id.tlbarHomepage);
        setSupportActionBar(tlbarHomepage);

        mDrawerLayout = findViewById(R.id.mDrawerLayout);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.Open,R.string.Close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rwPending = findViewById(R.id.rwPending);
        rwCompleted = findViewById(R.id.rwCompleted);

    }

    public static List<Information > getData(){
        List<Information> data = new ArrayList<>();
        int[] icons = {R.drawable.iconerror,R.drawable.iconerror,R.drawable.iconerror};
        String[] titles = {"Okan Dönmez","Okan Dönmez","Okan Dönmez"};
        String[] merchantName = {"Starbucks","Starbucks","Starbucks" };
        String[] dates = {"9 Tem", "9 Tem", "9 Tem"};
        String[] amounts = {"25.00","25.00","25.00"};


        for (int i = 0; i<titles.length && i<icons.length; i++){
            Information current = new Information();
            current.iconId = icons[i];
            current.title = titles[i];
            current.amount = amounts[i];
            current.expenseDate = setToDate(dates[i]);
            current.merchantName = merchantName[i];
            data.add(current);
        }


        return data;
    }

    public List<Information> getPendingExpenses(){

        RequestQueue queue = Volley.newRequestQueue(HomePage.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, pendingURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            String[] titles = new String[jsonArray.length()];
                            String[] amounts = new String[jsonArray.length()];
                            String[] merchants = new String[jsonArray.length()];
                            String[] dates = new String[jsonArray.length()];
                            String[] expenseIds = new String[jsonArray.length()];
                             int[] icons = new int[jsonArray.length()];
                            for (int i = 0; i <jsonArray.length(); i ++){
                                titles[i] = jsonArray.getJSONObject(i).getString("UserName");
                                amounts[i] = jsonArray.getJSONObject(i).getString("Amount");
                                merchants[i] = jsonArray.getJSONObject(i).getString("Merchant");
                                dates[i] = setToDate(jsonArray.getJSONObject(i).getString("Date"));
                                icons[i] = R.drawable.ulenslogo;
                                expenseIds[i] = jsonArray.getJSONObject(i).getString("Id");

                            }

                            for (int i = 0; i<jsonArray.length(); i++){
                                Information current = new Information();
                                current.iconId = icons[i];
                                current.title = titles[i];
                                current.amount = amounts[i];
                                current.expenseDate = dates[i];
                                current.merchantName = merchants[i];
                                current.expenseID = expenseIds[i];
                                pendingDatas.add(current);
                            }

                            adapter = new PendingAdapter(getApplicationContext(),pendingDatas);
                            rwPending.setAdapter(adapter);
                            rwPending.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


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

        Log.e("datasize",pendingDatas.size()+"");
        return pendingDatas;
    }


    public List<Information> getAcceptedExpenses(){

        RequestQueue queue = Volley.newRequestQueue(HomePage.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, acceptedURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            String[] titles = new String[jsonArray.length()];
                            String[] amounts = new String[jsonArray.length()];
                            String[] merchants = new String[jsonArray.length()];
                            String[] dates = new String[jsonArray.length()];
                            int[] icons = new int[jsonArray.length()];
                            String[] expenseIds = new String[jsonArray.length()];
                            for (int i = 0; i <jsonArray.length(); i ++){
                                titles[i] = jsonArray.getJSONObject(i).getString("UserName");
                                amounts[i] = jsonArray.getJSONObject(i).getString("Amount");
                                merchants[i] = jsonArray.getJSONObject(i).getString("Merchant");
                                dates[i] = setToDate(jsonArray.getJSONObject(i).getString("Date"));
                                icons[i] = R.drawable.ulenslogo;
                                expenseIds[i] = jsonArray.getJSONObject(i).getString("Id");

                            }

                            for (int i = 0; i<jsonArray.length(); i++){
                                Information current = new Information();
                                current.iconId = icons[i];
                                current.title = titles[i];
                                current.amount = amounts[i];
                                current.expenseDate = dates[i];
                                current.merchantName = merchants[i];
                                current.expenseID = expenseIds[i];
                                completedDatas.add(current);
                            }

                            getDeniedExpenses(completedDatas, jsonArray.length());


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


        Log.e("datasize",pendingDatas.size()+"");
        return pendingDatas;
    }

    public List<Information> getDeniedExpenses(final List<Information> completedExpenses, int length){

        RequestQueue queue = Volley.newRequestQueue(HomePage.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, deniedURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            String[] titles = new String[jsonArray.length()];
                            String[] amounts = new String[jsonArray.length()];
                            String[] merchants = new String[jsonArray.length()];
                            String[] dates = new String[jsonArray.length()];
                            int[] icons = new int[jsonArray.length()];
                            String[] expenseIds = new String[jsonArray.length()];
                            for (int i = 0; i <jsonArray.length(); i ++){
                                titles[i] = jsonArray.getJSONObject(i).getString("UserName");
                                amounts[i] = jsonArray.getJSONObject(i).getString("Amount");
                                merchants[i] = jsonArray.getJSONObject(i).getString("Merchant");
                                dates[i] = setToDate(jsonArray.getJSONObject(i).getString("Date"));
                                icons[i] = R.drawable.ulenslogo;
                                expenseIds[i] = jsonArray.getJSONObject(i).getString("Id");

                            }

                            for (int i = 0; i<jsonArray.length(); i++){
                                Information current = new Information();
                                current.iconId = icons[i];
                                current.title = titles[i];
                                current.amount = amounts[i];
                                current.expenseDate = dates[i];
                                current.merchantName = merchants[i];
                                current.expenseID = expenseIds[i];
                                completedExpenses.add(current);
                            }

                            adapter2 = new CompletedAdapter(getApplicationContext(),completedExpenses);
                            rwCompleted.setAdapter(adapter2);
                            rwCompleted.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


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


        Log.e("datasize",pendingDatas.size()+"");
        return pendingDatas;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int color){
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,color));
    }


    public void setToken(){
        sharedPref= getSharedPreferences("token", Context.MODE_PRIVATE);
        editor=sharedPref.edit();
        token = sharedPref.getString("token","000");
    }

    private  boolean checkAndRequestPermissions() {
        int camera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int loc = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
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

    public static boolean empty( final String s ) {
        // Null-safe, short-circuit evaluation.
        return s == null || s.trim().isEmpty();
    }

    public void setForget () {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isRemembered", false);
        editor.commit();
    }
}

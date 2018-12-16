package com.ulensapp.ulensapp.SupportClasses;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by okann on 26.12.2017.
 */

public class VolleyClass {

    private static VolleyClass mInstance;
    private RequestQueue requestQueue;
    private static Context mCtxt;

    private VolleyClass(Context context){

        mCtxt = context;
        requestQueue = getRequestQueue();

    }

    public static synchronized VolleyClass getInstance(Context context){

        if(mInstance == null){
            mInstance = new VolleyClass(context);
        }

        return mInstance;

    }




    public RequestQueue getRequestQueue(){

        if(requestQueue==null){

            requestQueue= Volley.newRequestQueue(mCtxt.getApplicationContext());
        }
        return requestQueue;

    }


    public <T>void addToRequestQueue(Request<T> request){
        requestQueue.add(request);
    }
}

package com.hvzhub.app;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Much of this code is taken directly from the volley tutorial:
 * http://developer.android.com/training/volley/requestqueue.html#singleton
 */
public class APIRequestQueue {
    private static APIRequestQueue mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private APIRequestQueue(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized APIRequestQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new APIRequestQueue(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}

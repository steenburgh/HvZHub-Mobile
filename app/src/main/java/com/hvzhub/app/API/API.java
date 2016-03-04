package com.hvzhub.app.API;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * A wrapper for the JSON API
 */
public class API {
    private static API mInstance;
    private static Context mCtx;
    private HvZHubClient mHvZHubClient = null;

    // 10.0.2.2 is the local computer's address for when android is running in an emulator
    public static final String PREFS_API = "prefs_api"; // SharedPreferences file for API
    public static final String PREFS_SESSION_ID = "sessionID";

    private static final String BASE_PATH = "http://10.0.2.2:8080/api/v1/";
    private static final String LOGIN = "login";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_SESSION_ID = "uuid";

    private String sessionID = null;

    /* This is implemented as a 'singleton'
     * This means that API is an object that can only be instantiated once
     * This also allows for API to have a context that is provided once, and remains the same
     * throughout the lifetime of the app
     */
    private API(Context context) {
        mCtx = context;
    }

    public static synchronized API getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new API(context);
        }
        return mInstance;
    }

    public HvZHubClient getHvZHubClient() {
        if (mHvZHubClient == null) {
            return ServiceGenerator.createService(HvZHubClient.class);
        }
        return mHvZHubClient;
    }

}

package com.hvzhub.app.API;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.hvzhub.app.LoginActivity;
import com.hvzhub.app.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
    public static final String PREFS_CHAPTER_URL = "chapterUrl";
    public static final String PREFS_GAME_ID = "gameID";

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

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

    public static Date dateFromUtcString(String dateStr) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(API.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.parse(dateStr);
    }

    public static String utcStringFromDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }
}

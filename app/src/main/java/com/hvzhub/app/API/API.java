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

    public static final String DATE_FORMATA = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_FORMATB = "yyyy-MM-dd'T'HH:mm:ss'Z'";

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
        return DateConverter.getInstance().deserialize(dateStr);
    }

    public static String utcStringFromDate(Date date) {
        return DateConverter.getInstance().serialize(date);
    }
}

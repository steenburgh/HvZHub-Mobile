package com.hvzhub.app;

import android.app.Application;

import com.hvzhub.app.DB.DB;

public class HvZHubApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DB.newInstance(this);
    }
}

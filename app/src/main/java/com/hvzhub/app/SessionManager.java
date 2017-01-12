package com.hvzhub.app;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.hvzhub.app.API.model.Login.Session;

public class SessionManager {
    private static SessionManager mInstance;
    private Application mAppCtx; // Force the use of an application as context to avoid memory leaks.

    private Session session;
    private OnLogoutListener onLogoutListener;

    private SessionManager(Application appCtx) {
        mAppCtx = appCtx;
    }

    public static synchronized void newInstance(Application appCtx) {
        mInstance = new SessionManager(appCtx);
    }

    public static synchronized SessionManager getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("SessionManager not initialized. Make sure to call newInstance() first to initialize it.");
        }
        return mInstance;
    }

    public @NonNull Session getSession() {
        if (session == null) {
            logout();
        }

        return session;
    }

    public void login(Session s) {
        this.session = s;
    }

    public void setOnLogoutListener(OnLogoutListener onLogoutListener) {
        this.onLogoutListener = onLogoutListener;
    }

    public void logout() {
        if (this.onLogoutListener != null) {
            this.onLogoutListener.onLogout();
        }
    }

    public static interface OnLogoutListener {
        void onLogout();
    }
}

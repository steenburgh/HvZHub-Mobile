package com.hvzhub.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.hvzhub.app.API.model.Login.Session;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Prefs.SessionPrefs;

import static android.content.Context.MODE_PRIVATE;

public class SessionManager {
    private static final String TAG = "SessionManager";

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
            String sessionID = mAppCtx.getSharedPreferences(SessionPrefs.NAME, MODE_PRIVATE)
                    .getString(SessionPrefs.PREFS_SESSION_ID, null);
            if (sessionID != null) {
                session = new Session(sessionID);
            } else {
                throw new RuntimeException("No Session found. Make sure to call createSession before getSession");
            }
        }

        return session;
    }

    public @NonNull Uuid getSessionUUID() {
        return new Uuid(getSession().uuid);
    }

    public boolean hasSession() {
        return session != null ||
                mAppCtx.getSharedPreferences(SessionPrefs.NAME, MODE_PRIVATE)
                        .contains(SessionPrefs.PREFS_SESSION_ID);
    }

    public void createSession(Session s) {
        this.session = s;
        SharedPreferences.Editor prefs = mAppCtx.getSharedPreferences(SessionPrefs.NAME, MODE_PRIVATE).edit();
        prefs.putString(SessionPrefs.PREFS_SESSION_ID, s.uuid);
        prefs.apply();
    }

    public void logout() {
        if (this.onLogoutListener != null) {
            this.onLogoutListener.onLogout();
        }
    }

    public void setOnLogoutListener(OnLogoutListener onLogoutListener) {
        this.onLogoutListener = onLogoutListener;
    }

    public static interface OnLogoutListener {
        void onLogout();
    }
}

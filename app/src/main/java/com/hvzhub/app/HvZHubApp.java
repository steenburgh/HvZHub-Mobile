package com.hvzhub.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.hvzhub.app.DB.DB;
import com.hvzhub.app.Prefs.GamePrefs;

public class HvZHubApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DB.newInstance(this);
        SessionManager.newInstance(this);
        SessionManager.getInstance().setOnLogoutListener(new SessionManager.OnLogoutListener() {
            @Override
            public void onLogout() {
                // Notify the user
                Toast t = Toast.makeText(
                        HvZHubApp.this,
                        R.string.you_have_been_logged_out,
                        Toast.LENGTH_LONG
                );
                t.show();

                // Clear notification subscriptions
                unsubscribeAll();

                // Clear *all* GamePrefs
                SharedPreferences.Editor editor = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();

                // Wipe the DB
                DB.getInstance().wipeDatabase();

                // Start the LoginActivity, closing everything else
                Intent i = new Intent(HvZHubApp.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
    }

    public void unsubscribeAll() {
        Intent intent = new Intent(this, GCMRegIntentService.class);
        intent.putExtra(GCMRegIntentService.CHAT_UNSUBSCRIBE_ALL, true);

        SharedPreferences prefs = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        intent.putExtra(GCMRegIntentService.ARGS_GAME_ID, gameId);

        startService(intent);
    }
}

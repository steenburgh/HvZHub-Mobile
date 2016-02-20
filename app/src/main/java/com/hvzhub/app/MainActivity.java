package com.hvzhub.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.hvzhub.app.API.API;

/**
 * Placeholder activity - Has no view + simply checks if the user is logged in + opens the
 * appropriate activity
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String sessionID = getSharedPreferences(API.PREFS_API, MODE_PRIVATE).getString(API.PREFS_SESSION_ID, null);
        if (sessionID != null) {
            finish();
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            Log.i("MainActivity", "SessionID found. Skipping login screen");
        } else {
            finish();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }

    }

}

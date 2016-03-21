package com.hvzhub.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.hvzhub.app.Prefs.GamePrefs;

/**
 * Placeholder activity - Has no view + simply checks if the user is logged in + opens the
 * appropriate activity
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Decide which activity to start
        String sessionID = getSharedPreferences(GamePrefs.PREFS_GAME, MODE_PRIVATE).getString(GamePrefs.PREFS_SESSION_ID, null);
        String chapterUrl = getSharedPreferences(GamePrefs.PREFS_GAME, MODE_PRIVATE).getString(GamePrefs.PREFS_CHAPTER_URL, null);
        int gameId = getSharedPreferences(GamePrefs.PREFS_GAME, MODE_PRIVATE).getInt(GamePrefs.PREFS_GAME_ID, -1);
        if (sessionID == null) {
            finish();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            Log.i("MainActivity", "SessionID not found. Showing login screen");
        } else if (chapterUrl == null) {
            finish();
            Intent intent = new Intent(this, ChapterSelectionActivity.class);
            startActivity(intent);
            Log.i("MainActivity", "ChapterUrl not found. Showing chapter selection screen");
        } else if (gameId == -1) {
            finish();
            Intent intent = new Intent(this, GameSelectionActivity.class);
            startActivity(intent);
            Log.i("MainActivity", "gameId not found. Showing chapter selection screen");
        } else {
            finish();
            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
            Log.i("MainActivity", "Skipping straight to GameActivity");
        }
    }

}

package com.hvzhub.app.Prefs;

/**
 * Preferences related to a current game in progress
 * These preferences are cleared when the user is logged out.
 */
public class GamePrefs {
    public static final String NAME = "gamePrefs";

    public static final String PREFS_SESSION_ID = "sessionID";
    public static final String PREFS_CHAPTER_URL = "chapterUrl";
    public static final String PREFS_GAME_ID = "gameID";
    public static final String PREFS_USER_ID = "userID";
    public static final String PREFS_IS_HUMAN = "isHuman";
    public static final String PREFS_IS_ADMIN = "isAdmin";
    public static final String PREFS_JUST_TURNED = "justTurned";
}
package com.hvzhub.app;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.hvzhub.app.Prefs.GCMRegistationPrefs;

import java.io.IOException;

public class GCMRegIntentService extends IntentService {

    private static final String TAG = "GCMRegIntentSvc";

    public static final String ARG_TO_SUBRCRIBE = "toSubscribe";
    public static final String ARG_TO_UNSUBRCRIBE = "toUnsubsrcibe";

    /**
     * Integer representing the ID for the game to operate on
     */
    public static final String ARGS_GAME_ID = "gameId";

    /**
     * Boolean representing whether or not the user is a human
     */
    public static final String ARG_IS_HUMAN = "isHuman";

    /**
     * Boolean representing whether or not the user is an admin
     */
    public static final String ARG_IS_ADMIN = "isAdmin";

    /**
     * Subscribe to the correct chat for the specified gameId and user type.
     * ARG_IS_HUMAN and ARG_IS_ADMIN and ARGS_GAME_ID must be provided.
     */
    public static final String CHAT_UPDATE_SUBSCRIPTIONS = "chatUpdate";

    /**
     * Unsubscribe from all chats for the gameId provided with ARGS_GAME_ID
     */
    public static final String CHAT_UNSUBSCRIBE_ALL = "chatUnsubAll";

    private String[] toSubscribe;
    private String[] toUnsubscribe;

    public GCMRegIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getExtras() == null) {
            throw new RuntimeException("GCMRegIntentService must be called with extras");
        }
        Bundle extras = intent.getExtras();


        /*********** Update mode **********/
        if (intent.getExtras().getBoolean(CHAT_UPDATE_SUBSCRIPTIONS, false)) {
            if (!extras.containsKey(ARGS_GAME_ID) || !extras.containsKey(ARG_IS_HUMAN) || !extras.containsKey(ARG_IS_ADMIN)) {
                throw new IllegalArgumentException("Invalid arguments for GCMRegIntentService running in CHAT_UPDATE_SUBSCRIPTIONS mode.\n Make sure to specify: ARGS_GAME_ID, ARG_IS_HUMAN and ARG_IS_ADMIN");
            }
            int gameId = extras.getInt(ARGS_GAME_ID, -1);
            boolean isHuman = extras.getBoolean(ARG_IS_ADMIN, false);
            boolean isAdmin = extras.getBoolean(ARG_IS_HUMAN, false);

            Log.d(TAG, String.format("Updating chat subscriptions for gameId: %s", gameId));

            if (isAdmin) {
                toSubscribe = new String[2];
                toSubscribe[0] = String.format("games_%d_chat_human", gameId);
                toSubscribe[1] = String.format("games_%d_chat_zombie", gameId);
            } else {
                toSubscribe = new String[1];
                toSubscribe[0] =  String.format(
                    "games_%d_chat_%s",
                    gameId,
                    isHuman ? "human" : "zombie"
                );

                // Make sure to unsubscribe from the other side's chat
                toUnsubscribe = new String[1];
                toUnsubscribe[0] = String.format(
                    "games_%d_chat_%s",
                    gameId,
                    isHuman ? "zombie" : "human"
                );
            }
        }

        /*********** Unsubscribe all mode **********/
        else if (intent.getExtras().getBoolean(CHAT_UNSUBSCRIBE_ALL, false)) {
            if (!extras.containsKey(ARGS_GAME_ID)) {
                throw new IllegalArgumentException("Invalid arguments for GCMRegIntentService running in CHAT_UNSUBSCRIBE_ALL mode:\nGame Id not found. Make sure a gameId has been provided as an argument before attempting to unsubscribe");
            }

            int gameId = extras.getInt(ARGS_GAME_ID, -1);
            Log.d(TAG, String.format("Unsubscribing from everything for current game ID: %s", gameId));

            toUnsubscribe = new String[2];
            toUnsubscribe[0] = String.format("games_%d_chat_human", gameId);
            toUnsubscribe[1] = String.format("games_%d_chat_zombie", gameId);
        }

        /******** Normal mode **********/
        else {
            toSubscribe = extras.getStringArray(ARG_TO_SUBRCRIBE);
            toUnsubscribe = extras.getStringArray(ARG_TO_UNSUBRCRIBE);
            if (toSubscribe == null && toUnsubscribe == null) {
                throw new IllegalArgumentException("No valid arguments found for GCMRegIntentService");
            }
        }


        /////////////////////////////////// Register and Subscribe /////////////////////////////////
        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            // Subscribe to topic channels
            updateSubscriptions(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.edit().putBoolean(GCMRegistationPrefs.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.e(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.edit().putBoolean(GCMRegistationPrefs.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(GCMRegistationPrefs.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }


    private void updateSubscriptions(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);

        if (toSubscribe != null) {
            for (String topic : toSubscribe) {
                pubSub.subscribe(token, "/topics/" + topic, null);
                Log.d(TAG, String.format("Subscribed to: /topics/%s", topic));
            }
        }
        if (toUnsubscribe != null) {
            for (String topic : toUnsubscribe) {
                pubSub.unsubscribe(token, "/topics/" + topic);
                Log.d(TAG, String.format("Unsubscribed from: /topics/%s", topic));
            }
        }

        if (toSubscribe == null && toUnsubscribe == null){
            Log.w(TAG, "No subscription changes were made. Did you forget to specify arguments?");
        }



    }
}

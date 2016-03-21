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

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    public static final String ARG_TO_SUBRCRIBE = "toSubscribe";
    public static final String ARG_TO_UNSUBRCRIBE = "toUnsubsrcibe";
    private String[] toSubscribe;
    private String[] toUnsubscribe;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            toSubscribe = extras.getStringArray(ARG_TO_SUBRCRIBE);
            toUnsubscribe = extras.getStringArray(ARG_TO_UNSUBRCRIBE);
        } else {
            throw new RuntimeException("RegistrationIntentService must be called with arguments.");
        }

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
            sharedPreferences.edit().putBoolean(GCMRegistationPrefs.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.e(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(GCMRegistationPrefs.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(GCMRegistationPrefs.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }


    private void updateSubscriptions(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);

        for (String topic : toSubscribe) {
            pubSub.subscribe(token, "/topics/" + topic, null);
            Log.d(TAG, String.format("Subscribed to: /topics/%s", topic));
        }

        for (String topic : toUnsubscribe) {
            pubSub.unsubscribe(token, "/topics/" + topic);
            Log.d(TAG, String.format("Unsubscribed from: /topics/%s", topic));
        }
    }

}

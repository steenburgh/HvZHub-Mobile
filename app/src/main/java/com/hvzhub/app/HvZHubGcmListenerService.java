package com.hvzhub.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.hvzhub.app.API.API;
import com.hvzhub.app.API.model.Chat.Message;
import com.hvzhub.app.DB.DB;
import com.hvzhub.app.Prefs.ChatPrefs;
import com.hvzhub.app.Prefs.GamePrefs;

import java.text.ParseException;
import java.util.Date;

public class HvZHubGcmListenerService extends GcmListenerService {
    private static final String TAG = "HvZHubGcmListener";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String rawData = data.toString();
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Raw Data: " + rawData);

        if (from.startsWith("/topics/")) {
            handleChatMessage(data);
        } else {
            // normal downstream message.
        }
    }
    // [END receive_message]

    private boolean handleChatMessage(Bundle data) {
        String uidString = data.getString("uid");
        int userId;
        if (uidString != null) {
            userId = Integer.parseInt(uidString);
        } else {
            Log.e(TAG, "No user ID received for Chat message");
            return false;
        }

        String name = data.getString("n");
        if (name != null) {
            name = Html.fromHtml(name).toString();
        }

        String message = data.getString("x");
        if (message != null) {
            message = Html.fromHtml(message).toString();
        }

        String msgIdString = data.getString("uid");
        int msgId;
        if (msgIdString != null) {
            msgId = Integer.parseInt(msgIdString);
        } else {
            Log.e(TAG, "No message ID received for Chat message");
            return false;
        }


        String dateStr = data.getString("t");
        Date date;
        try {
            date = API.dateFromUtcString(dateStr);
        } catch (ParseException e) {
            Log.e(TAG, String.format("Error parsing date from string: %s", dateStr));
            return false;
        }

        boolean isHuman = getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE).getBoolean(GamePrefs.PREFS_IS_HUMAN, false);
        DB.getInstance(this).addMessageToChat(new Message(
                        userId,
                        name,
                        message,
                        date,
                        msgId
                ),
                isHuman ? DB.HUMAN_CHAT : DB.ZOMBIE_CHAT
        );


        boolean chatIsOpen = getSharedPreferences(ChatPrefs.NAME, Context.MODE_PRIVATE).getBoolean(ChatPrefs.IS_OPEN, false);
        SharedPreferences.Editor editor = getSharedPreferences(ChatPrefs.NAME, Context.MODE_PRIVATE).edit();
        editor.apply();

        Intent messageReceived = new Intent(ChatPrefs.MESSAGE_RECEIVED_BROADCAST);
        Log.d(TAG, "Sending broadcast: message received ");
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageReceived);

        boolean notificationsEnabled = getSharedPreferences(ChatPrefs.NAME, Context.MODE_PRIVATE).getBoolean(ChatPrefs.NOTIFICATIONS_ENABLED, false);
        if (!chatIsOpen && notificationsEnabled) {
            sendNotification(name, message);
        }

        return true;
    }


    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Launch the chat activity if clicked
        Bundle b = new Bundle();
        b.putInt(GameActivity.ARG_FRAGMENT_NAME, GameActivity.CHAT_FRAGMENT);
        intent.putExtras(b);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);



        // Create the notification
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}

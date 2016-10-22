package com.hvzhub.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.hvzhub.app.API.API;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.model.Chat.Message;
import com.hvzhub.app.API.model.Games.Record;
import com.hvzhub.app.API.model.Games.RecordContainer;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.DB.DB;
import com.hvzhub.app.Prefs.ChatPrefs;
import com.hvzhub.app.Prefs.GamePrefs;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HvZHubGcmListenerService extends GcmListenerService implements OnRefreshIsHumanListener {
    private static final String TAG = "HvZHubGcmListener";
    private static final String GROUP_ZOMBIE_CHAT = "chatZombie";
    private static final String GROUP_HUMAN_CHAT = "chatHuman";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, final Bundle data) {
        String rawData = data.toString();

        if (from.startsWith("/topics/")) {
            final String topic = from.split("/")[2];
            OnRefreshIsHuman(new OnIsHumanRefreshedListener() {
                @Override
                public void OnIsHumanRefreshed() {
                    handleChatMessage(data, topic);
                }
            });

        } else {
            // normal downstream message.
        }
    }
    // [END receive_message]

    private boolean handleChatMessage(Bundle data, String topic) {
        boolean isHuman = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).getBoolean(GamePrefs.PREFS_IS_HUMAN, false);
        String[] topicSplit = topic.split("_");
        String topicTeamStr = topicSplit[topicSplit.length - 1];
        boolean topicTeamIsHuman = topicTeamStr.equals("human");

        // Check for the correct team
        if (topicTeamIsHuman != isHuman) {
            updateNotificationSubscriptions();
            return false;
        }

        String uidString = data.getString("uid");
        int userId;
        if (uidString != null) {
            userId = Integer.parseInt(uidString);
        } else {
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

        String msgIdString = data.getString("id");
        int msgId;
        if (msgIdString != null) {
            msgId = Integer.parseInt(msgIdString);
        } else {
            return false;
        }


        String dateStr = data.getString("t");
        Date date;
        try {
            date = API.dateFromUtcString(dateStr);
        } catch (ParseException e) {
            return false;
        }

        DB.getInstance().addMessageToChat(new Message(
                        userId,
                        name,
                        message,
                        date,
                        msgId
                ),
                topicTeamIsHuman ? DB.HUMAN_CHAT : DB.ZOMBIE_CHAT
        );


        boolean chatIsOpen = getSharedPreferences(ChatPrefs.NAME, Context.MODE_PRIVATE).getBoolean(ChatPrefs.IS_OPEN, false);
        SharedPreferences.Editor editor = getSharedPreferences(ChatPrefs.NAME, Context.MODE_PRIVATE).edit();
        editor.apply();

        Intent messageReceived = new Intent(ChatPrefs.MESSAGE_RECEIVED_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageReceived);

        boolean notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.NOTIFICATIONS_ENABLED, false);

        if (!chatIsOpen && notificationsEnabled) {
            sendNotification(name, message, topicTeamIsHuman);
        }

        return true;
    }

    @Override
    public void OnRefreshIsHuman(final OnRefreshIsHumanListener.OnIsHumanRefreshedListener listener) {
        HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();
        SharedPreferences prefs = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE);
        String uuid = prefs.getString(GamePrefs.PREFS_SESSION_ID, null);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        Call<RecordContainer> call = client.getMyRecord(new Uuid(uuid), gameId);
        call.enqueue(new Callback<RecordContainer>() {
            @Override
            public void onResponse(Call<RecordContainer> call, Response<RecordContainer> response) {
                if (response.isSuccessful()) {
                    Record r = response.body().record;
                    SharedPreferences.Editor editor = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).edit();
                    editor.putBoolean(GamePrefs.PREFS_IS_HUMAN, r.status == Record.HUMAN);
                    editor.apply();

                    listener.OnIsHumanRefreshed();
                } else {
                    listener.OnIsHumanRefreshed();
                }
            }

            @Override
            public void onFailure(Call<RecordContainer> call, Throwable t) {
                listener.OnIsHumanRefreshed(); // Call this anyways. we don't want to lose notifications
            }
        });
    }

    public void updateNotificationSubscriptions() {
        Intent intent = new Intent(this, GCMRegIntentService.class);

        // Configure the service to be in update mode
        intent.putExtra(GCMRegIntentService.CHAT_UPDATE_SUBSCRIPTIONS, true);

        // Add arguments
        SharedPreferences prefs = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        boolean isHuman = prefs.getBoolean(GamePrefs.PREFS_IS_HUMAN, false);
        boolean isAdmin = prefs.getBoolean(GamePrefs.PREFS_IS_ADMIN, false);
        intent.putExtra(GCMRegIntentService.ARGS_GAME_ID, gameId);
        intent.putExtra(GCMRegIntentService.ARG_IS_HUMAN, isHuman);
        intent.putExtra(GCMRegIntentService.ARG_IS_ADMIN, isAdmin);

        startService(intent);
    }


    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String title, String message, boolean isHumanChat) {
        List<com.hvzhub.app.DB.Message> messageList = DB.getInstance().getMessages(isHumanChat ? DB.HUMAN_CHAT : DB.ZOMBIE_CHAT);

        Intent intent = new Intent(this, GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Launch the chat activity if clicked
        Bundle b = new Bundle();
        b.putInt(GameActivity.ARG_FRAGMENT_NAME, GameActivity.CHAT_FRAGMENT);
        intent.putExtras(b);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);



        /************ Create the notification *************/
        // Setup ringtone
        Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String soundUriString = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.NOTIFICATIONS_RINGTONE, null);
        Uri soundUri = (soundUriString == null) ? defaultUri : Uri.parse(soundUriString);

        // Setup Vibrate
        boolean vibrate = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SettingsActivity.NOTIFICATIONS_VIBRATE, false);
        int defaults;
        if (vibrate) {
            defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
        } else {
            defaults = Notification.DEFAULT_LIGHTS;
        }

        // Setup large icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_monocolor_nocircles)
                .setLargeIcon(largeIcon)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Make this an expandable notification
                .setGroup(isHumanChat ? GROUP_HUMAN_CHAT : GROUP_ZOMBIE_CHAT)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationBuilder.setSound(soundUri); // Ringtone
        notificationBuilder.setDefaults(defaults); // Vibrate + Notification light



        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(messageList.size(), notificationBuilder.build());

        if (messageList.size() > 1) {
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                    .setBigContentTitle(String.format("%d new messages", messageList.size()))
                    .setSummaryText(isHumanChat ? getString(R.string.human_chat) : getString(R.string.zombie_chat));

            for (int i = messageList.size() - 1; i >= 0; i--) {
                com.hvzhub.app.DB.Message msg  = messageList.get(i);
                style.addLine(String.format("%s   %s", msg.getName(), msg.getMessage()));
            }


            com.hvzhub.app.DB.Message firstMsg = messageList.get(messageList.size() - 1);
            NotificationCompat.Builder notificationBuilder2 = new NotificationCompat.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_launcher_monocolor_nocircles)
                    .setLargeIcon(largeIcon)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .setContentTitle(String.format("%d new messages", messageList.size()))
                    .setContentText(String.format("%s    %s", firstMsg.getName(), firstMsg.getMessage()))
                    .setStyle(style)
                    .setGroup(isHumanChat ? GROUP_HUMAN_CHAT : GROUP_ZOMBIE_CHAT)
                    .setGroupSummary(true)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSound(soundUri)
                    .setDefaults(defaults);

            notificationManager.notify(0, notificationBuilder2.build());
        }

    }
}

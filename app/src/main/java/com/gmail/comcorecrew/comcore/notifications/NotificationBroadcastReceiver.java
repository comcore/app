package com.gmail.comcorecrew.comcore.notifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gmail.comcorecrew.comcore.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * BroadcastReceiver to send a notification at a specific time.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the notification JSON and ID from the Intent
        String jsonString = intent.getStringExtra("json");
        int id = intent.getIntExtra("id", 0);

        // Parse the JSON to create a ScheduledNotification object
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        ScheduledNotification scheduled = ScheduledNotification.fromJson(json);

        // Create a notification with the required information
        Notification notification = new NotificationCompat.Builder(context, scheduled.channel)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle(scheduled.title)
                .setContentText(scheduled.text)
                .setWhen(scheduled.timestamp)
                .setPriority(scheduled.priority)
                .build();

        // Send the notification
        NotificationManagerCompat.from(context).notify(id, notification);
    }
}
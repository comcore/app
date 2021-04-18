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

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String jsonString = intent.getStringExtra("json");
        int id = intent.getIntExtra("id", 0);

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        ScheduledNotification scheduled = ScheduledNotification.fromJson(json);

        Notification notification = new NotificationCompat.Builder(context, scheduled.channel)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle(scheduled.title)
                .setContentText(scheduled.text)
                .setWhen(scheduled.timestamp)
                .setPriority(scheduled.priority)
                .build();

        NotificationManagerCompat.from(context).notify(id, notification);
    }
}
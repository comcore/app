package com.gmail.comcorecrew.comcore.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;
import com.gmail.comcorecrew.comcore.server.NotificationListener;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.Collection;

public class NotificationHandler implements NotificationListener {
    private static final String CHANNEL_MESSAGE = "message";
    private static final String CHANNEL_INVITE = "invite";
    private static final String CHANNEL_STATUS = "status";

    private static int uniqueId = (int) System.currentTimeMillis();

    private final Context context;
    private final NotificationManagerCompat manager;

    public NotificationHandler(Context context) {
        this.context = context;
        this.manager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(int name, int description, String channelID, int importance) {
        CharSequence nameStr = context.getString(name);
        String descriptionStr = context.getString(description);
        NotificationChannel channel = new NotificationChannel(channelID, nameStr, importance);
        channel.setDescription(descriptionStr);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        createNotificationChannel(
                R.string.ch_name_message, R.string.ch_desc_message,
                CHANNEL_MESSAGE, NotificationManager.IMPORTANCE_HIGH);

        createNotificationChannel(
                R.string.ch_name_invite, R.string.ch_desc_invite,
                CHANNEL_INVITE, NotificationManager.IMPORTANCE_DEFAULT);

        createNotificationChannel(
                R.string.ch_name_status, R.string.ch_desc_status,
                CHANNEL_STATUS, NotificationManager.IMPORTANCE_LOW);
    }

    private void notify(Notification notification) {
        manager.notify(uniqueId++, notification);
    }

    @Override
    public void onReceiveMessage(MessageEntry message) {
        notify(new NotificationCompat.Builder(context, CHANNEL_MESSAGE)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle(message.sender.name)
                .setContentText(message.contents)
                .setWhen(message.timestamp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build());
    }

    @Override
    public void onInvitedToGroup(GroupInviteEntry invite) {
        notify(new NotificationCompat.Builder(context, CHANNEL_INVITE)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle(invite.inviter)
                .setContentText("Invited you to join \"" + invite.name + "\"")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build());
    }

    @Override
    public void onRoleChanged(GroupID group, GroupRole role) {
        notify(new NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle("Role Changed")
                .setContentText("You have become a " + role)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build());
    }

    @Override
    public void onMuteChanged(GroupID group, boolean muted) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(R.drawable.receivedmsg)
            .setPriority(NotificationCompat.PRIORITY_LOW);

        if (muted) {
            builder.setContentTitle("Muted").setContentText("You have been muted");
        } else {
            builder.setContentTitle("Unmuted").setContentText("You have been unmuted");
        }

        notify(builder.build());
    }

    @Override
    public void onKicked(GroupID group) {
        notify(new NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle("Kicked")
                .setContentText("You have been kicked")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build());
    }

    @Override
    public void onLoggedOut() {}

    @Override
    public Collection<? extends NotificationListener> getChildren() {
        return MainFragment.groups;
    }
}

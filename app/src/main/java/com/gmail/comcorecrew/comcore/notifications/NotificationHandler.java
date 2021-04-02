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
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.fragments.MainFragment;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.info.ModuleInfo;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Handles displaying notifications by forwarding them to Android.
 */
public class NotificationHandler implements NotificationListener {
    // Android notification channel identifiers
    private static final String CHANNEL_MESSAGE = "message";
    private static final String CHANNEL_TASK = "task";
    private static final String CHANNEL_INVITE = "invite";
    private static final String CHANNEL_STATUS = "status";

    // A unique ID to give a notification
    private static int uniqueId = (int) System.currentTimeMillis();

    // The context and notification manager for displaying notifications
    private final Context context;
    private final NotificationManagerCompat manager;

    /**
     * Create a NotificationHandler in a Context
     * @param context the Context
     */
    public NotificationHandler(Context context) {
        this.context = context;
        this.manager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }

    /**
     * Create an Android notification channel, registering it with the system.
     *
     * @param name        the name of the channel
     * @param description the description of the channel
     * @param channelID   the channel identifier
     * @param importance  the importance level
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(int name, int description, String channelID, int importance) {
        CharSequence nameStr = context.getString(name);
        String descriptionStr = context.getString(description);
        NotificationChannel channel = new NotificationChannel(channelID, nameStr, importance);
        channel.setDescription(descriptionStr);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Create all necessary notification channels before the app starts.
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        createNotificationChannel(
                R.string.ch_name_message, R.string.ch_desc_message,
                CHANNEL_MESSAGE, NotificationManager.IMPORTANCE_HIGH);

        createNotificationChannel(
                R.string.ch_name_task, R.string.ch_desc_task,
                CHANNEL_TASK, NotificationManager.IMPORTANCE_HIGH);

        createNotificationChannel(
                R.string.ch_name_invite, R.string.ch_desc_invite,
                CHANNEL_INVITE, NotificationManager.IMPORTANCE_DEFAULT);

        createNotificationChannel(
                R.string.ch_name_status, R.string.ch_desc_status,
                CHANNEL_STATUS, NotificationManager.IMPORTANCE_LOW);
    }

    /**
     * Send a notification, giving it a unique identifier.
     *
     * @param notification the notification to send
     */
    private void notify(Notification notification) {
        manager.notify(uniqueId++, notification);
    }

    @Override
    public void onReceiveMessage(MessageEntry message) {
        Module module = GroupStorage.getModule(message.id.module);
        if (module == null) {
            return;
        }

        // Check for mentions for the current user in the notification
        String name = ServerConnector.getUser().name;
        boolean mentioned = false;
        for (ChatMention mention : ChatMention.parseMentions(message.contents)) {
            if (mention.mentionsUser(name)) {
                if (module.isMentionMuted()) {
                    return;
                }
                mentioned = true;
                break;
            }
        }

        // If there were no mentions, use the plain muted status
        if (!mentioned && module.isMuted()) {
            return;
        }

        UserStorage.lookup(message.sender, user ->
            notify(new NotificationCompat.Builder(context, CHANNEL_MESSAGE)
                    .setSmallIcon(R.drawable.receivedmsg)
                    .setContentTitle(module.getName())
                    .setContentText(user.getName() + ": " + message.contents)
                    .setWhen(message.timestamp)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()));
    }

    @Override
    public void onTaskAdded(TaskEntry task) {
        Module module = GroupStorage.getModule(task.id.module);
        if (module == null || module.isMuted()) {
            return;
        }

        UserStorage.lookup(task.owner, user ->
            notify(new NotificationCompat.Builder(context, CHANNEL_TASK)
                    .setSmallIcon(R.drawable.receivedmsg)
                    .setContentTitle(module.getName())
                    .setContentText("Added: " + task.description)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()));
    }

    @Override
    public void onTaskUpdated(TaskEntry task) {
        if (!task.completed) {
            // If the task was marked un-completed, don't send a notification
            return;
        }

        Module module = GroupStorage.getModule(task.id.module);
        if (module == null || module.isMuted()) {
            return;
        }

        UserStorage.lookup(task.owner, user ->
            notify(new NotificationCompat.Builder(context, CHANNEL_TASK)
                    .setSmallIcon(R.drawable.receivedmsg)
                    .setContentTitle(module.getName())
                    .setContentText("Completed: " + task.description)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()));
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
        Group info = GroupStorage.getGroup(group);
        if (info == null) {
            return;
        }

        notify(new NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle(info.getName())
                .setContentText("You have become a " + role)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build());
    }

    @Override
    public void onMuteChanged(GroupID group, boolean muted) {
        Group info = GroupStorage.getGroup(group);
        if (info == null) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle(info.getName())
                .setPriority(NotificationCompat.PRIORITY_LOW);

        if (muted) {
            builder.setContentText("You have been muted");
        } else {
            builder.setContentText("You have been unmuted");
        }

        notify(builder.build());
    }

    @Override
    public void onKicked(GroupID group, String name) {
        notify(new NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle(name)
                .setContentText("You have been kicked")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build());
    }

    @Override
    public void onLoggedIn(UserInfo user) {
        try {
            AppData.init(user, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<? extends NotificationListener> getChildren() {
        return AppData.groups;
    }
}
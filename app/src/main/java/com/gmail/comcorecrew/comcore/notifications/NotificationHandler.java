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
import com.gmail.comcorecrew.comcore.enums.TaskStatus;
import com.gmail.comcorecrew.comcore.helpers.ChatMention;
import com.gmail.comcorecrew.comcore.server.LoginToken;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Handles displaying notifications by forwarding them to Android.
 */
public final class NotificationHandler implements NotificationListener {
    // Android notification channel identifiers
    public static final String CHANNEL_MESSAGE = "message";
    public static final String CHANNEL_TASK = "task";
    public static final String CHANNEL_EVENT = "event";
    public static final String CHANNEL_INVITE = "invite";
    public static final String CHANNEL_STATUS = "status";

    // A unique ID to give a notification
    private static int uniqueId = (int) (System.currentTimeMillis() >> 3);

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
                R.string.ch_name_event, R.string.ch_desc_event,
                CHANNEL_EVENT, NotificationManager.IMPORTANCE_HIGH);

        createNotificationChannel(
                R.string.ch_name_invite, R.string.ch_desc_invite,
                CHANNEL_INVITE, NotificationManager.IMPORTANCE_DEFAULT);

        createNotificationChannel(
                R.string.ch_name_status, R.string.ch_desc_status,
                CHANNEL_STATUS, NotificationManager.IMPORTANCE_LOW);
    }

    /**
     * Get a unique identifier for a notification.
     *
     * @return a unique identifier
     */
    public static int getUniqueId() {
        return uniqueId++;
    }

    /**
     * Send a notification, giving it a unique identifier.
     *
     * @param notification the notification to send
     */
    private void notify(Notification notification) {
        manager.notify(getUniqueId(), notification);
    }

    @Override
    public void onReceiveMessage(MessageEntry message) {
        Module module = GroupStorage.getModule(message.id.module);
        if (module == null) {
            return;
        }

        // Check for mentions for the current user in the notification
        String name = AppData.self.getName();
        boolean mentioned = false;
        List<ChatMention> mentions = ChatMention.parseMentions(message.contents);
        for (ChatMention mention : mentions) {
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

        // Format the mentions as if in a message
        Group group = module.getGroup();
        String formatted = ChatMention.formatMentions(message.contents, group, mentions).toString();

        UserStorage.lookup(message.sender, user ->
            notify(new NotificationCompat.Builder(context, CHANNEL_MESSAGE)
                    .setSmallIcon(R.drawable.receivedmsg)
                    .setContentTitle(module.getName())
                    .setContentText(user.getName() + ": " + formatted)
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

        UserStorage.lookup(task.creator, user ->
            notify(new NotificationCompat.Builder(context, CHANNEL_TASK)
                    .setSmallIcon(R.drawable.receivedmsg)
                    .setContentTitle(module.getName())
                    .setContentText(user.getName() + " added: " + task.description)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()));
    }

    @Override
    public void onTaskUpdated(TaskEntry task) {
        if (task.getStatus() != TaskStatus.COMPLETED) {
            // If the task wasn't marked completed, don't send a notification
            return;
        }

        Module module = GroupStorage.getModule(task.id.module);
        if (module == null || module.isMuted()) {
            return;
        }

        UserStorage.lookup(task.completer, user ->
            notify(new NotificationCompat.Builder(context, CHANNEL_TASK)
                    .setSmallIcon(R.drawable.receivedmsg)
                    .setContentTitle(module.getName())
                    .setContentText(user.getName() + " completed: " + task.description)
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
        Group info = AppData.getGroup(group);
        if (info == null) {
            return;
        }

        notify(new NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle(info.getDisplayName())
                .setContentText("You have become a " + role)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build());
    }

    @Override
    public void onMuteChanged(GroupID group, boolean muted) {
        Group info = AppData.getGroup(group);
        if (info == null) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(R.drawable.receivedmsg)
                .setContentTitle(info.getDisplayName())
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
    public void onLoggedIn(UserInfo user, LoginToken token) {
        try {
            AppData.init(user, token, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<? extends NotificationListener> getChildren() {
        return AppData.getGroups();
    }
}
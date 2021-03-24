package com.gmail.comcorecrew.comcore.notifications;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.entry.*;
import com.gmail.comcorecrew.comcore.server.id.*;

import java.util.Collection;

/**
 * Interface containing methods that will be called by the ServerConnector when it receives a
 * notification from the server. Notification listeners are always called on the main thread.
 */
public interface NotificationListener {
    /**
     * Handle a message that was received by the server. If the message isn't handled, it will still
     * appear in the chat when it is next refreshed, so it is fine if it is ignored sometimes.
     *
     * @param message the message received from the server
     */
    default void onReceiveMessage(MessageEntry message) {}

    /**
     * Handle a message's contents being updated. The new contents will never be null.
     *
     * @param message     the message being updated
     * @param newContents the new contents of the message
     * @param timestamp   when the message was updated
     */
    default void onMessageUpdated(MessageID message, String newContents, long timestamp) {}

    /**
     * Handle a new task being added to a task list.
     *
     * @param task the task received from the server
     */
    default void onTaskAdded(TaskEntry task) {}

    /**
     * Handle a task's completion status being updated.
     *
     * @param task      the task being updated
     * @param completed whether the task was completed
     */
    default void onTaskUpdated(TaskEntry task, boolean completed) {}

    /**
     * Handle a task being deleted.
     *
     * @param task the task being deleted
     */
    default void onTaskDeleted(TaskID task) {}

    /**
     * Handle being sent a group invite.
     *
     * @param invite the invitation to the group
     */
    default void onInvitedToGroup(GroupInviteEntry invite) {}

    /**
     * Handle the user's role in a group being changed.
     *
     * @param group the group
     * @param role  the newly assigned role
     */
    default void onRoleChanged(GroupID group, GroupRole role) {}

    /**
     * Handle the user's muted status in a group being changed.
     *
     * @param group the group
     * @param muted the muted status
     */
    default void onMuteChanged(GroupID group, boolean muted) {}

    /**
     * Handle being kicked from a group.
     *
     * @param group the group
     */
    default void onKicked(GroupID group) {}

    /**
     * Handle being logged out forcibly by the server. This could be used to go back to the login
     * menu for the user to log back into the app.
     */
    default void onLoggedOut() {}

    /**
     * Gets a list of children notification listeners to also notify. May be null.
     */
    default Collection<? extends NotificationListener> getChildren() {
        return null;
    }
}

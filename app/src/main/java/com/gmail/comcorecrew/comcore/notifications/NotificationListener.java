package com.gmail.comcorecrew.comcore.notifications;

import com.gmail.comcorecrew.comcore.server.LoginToken;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.entry.*;
import com.gmail.comcorecrew.comcore.server.id.*;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

import java.util.Collection;

/**
 * Interface containing methods that will be called by the ServerConnector when it receives a
 * notification from the server. Notification listeners are always called on the main thread.
 *
 * @see ServerConnector.Notification
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
     * Handle a message's contents being updated. The new contents will never be null. This method
     * will not receive updates to reactions in messages, only the contents of the message itself.
     *
     * @param message the updated message
     */
    default void onMessageUpdated(MessageEntry message) {}

    /**
     * Handle a reaction being added to a message.
     *
     * @param message  the message to update
     * @param reaction the reaction to add
     */
    default void onReactionAdded(MessageID message, ReactionEntry reaction) {}

    /**
     * Handle a reaction being removed from a message.
     *
     * @param message  the message to update
     * @param reaction the reaction to remove
     */
    default void onReactionRemoved(MessageID message, ReactionEntry reaction) {}

    /**
     * Handle a new task being added to a task list.
     *
     * @param task the task received from the server
     */
    default void onTaskAdded(TaskEntry task) {}

    /**
     * Handle a task's status being updated.
     *
     * @param task the updated task
     */
    default void onTaskUpdated(TaskEntry task) {}

    /**
     * Handle a task being deleted.
     *
     * @param task the task being deleted
     */
    default void onTaskDeleted(TaskID task) {}

    /**
     * Handle a new event being added to a calendar.
     *
     * @param event the event received from the server
     */
    default void onEventAdded(EventEntry event) {}

    /**
     * Handle a user's event being approved by a moderator.
     *
     * @param event the event being approved
     */
    default void onEventApproved(EventID event) {}

    /**
     * Handle an event being deleted.
     *
     * @param event the event being deleted
     */
    default void onEventDeleted(EventID event) {}

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
     * @param name  the name of the group
     */
    default void onKicked(GroupID group, String name) {}

    /**
     * Handle the user logging in successfully. This could be used to update the cached data.
     *
     * @param user  the information of the user
     * @param token the login token of the user
     */
    default void onLoggedIn(UserInfo user, LoginToken token) {}

    /**
     * Handle being logged out forcibly by the server. This could be used to go back to the login
     * menu for the user to log back into the app.
     */
    default void onLoggedOut() {}

    /**
     * Gets a list of children notification listeners to also notify. May be null.
     *
     * @see ServerConnector.Notification
     */
    default Collection<? extends NotificationListener> getChildren() {
        return null;
    }
}

package com.gmail.comcorecrew.comcore.server;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

/**
 * Interface containing methods that will be called by the ServerConnector when it receives a
 * notification from the server. Notification listeners are always called on the main thread.
 */
public interface NotificationListener {
    /**
     * Handle a message that was received by the server. If the message isn't handled, it will still
     * appear in the chat when it is next refreshed, so it is fine if it is ignored sometimes.
     *
     * @param message the message the server received
     */
    void onReceiveMessage(MessageEntry message);

    /**
     * Handle being sent a group invite.
     *
     * @param invite the invitation to the group
     */
    void onInvitedToGroup(GroupInviteEntry invite);

    /**
     * Handle the user's role in a group being changed.
     *
     * @param group the group
     * @param role  the newly assigned role
     */
    void onRoleChanged(GroupID group, GroupRole role);

    /**
     * Handle the user's muted status in a group being changed.
     *
     * @param group the group
     * @param muted the muted status
     */
    void onMuteChanged(GroupID group, boolean muted);

    /**
     * Handle being kicked from a group.
     *
     * @param group the group
     */
    void onKicked(GroupID group);

    /**
     * Handle being logged out forcibly by the server. This could be used to go back to the login
     * menu for the user to log back into the app.
     */
    void onLoggedOut();
}
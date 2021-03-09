package com.gmail.comcorecrew.comcore.server;

import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;

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
     * Handle being logged out forcibly by the server. This could be used to go back to the login
     * menu for the user to log back into the app.
     */
    void onLoggedOut();
}
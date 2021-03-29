package com.gmail.comcorecrew.comcore.server.connection.thread;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.notifications.NotificationListener;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.gmail.comcorecrew.comcore.server.connection.ServerMsg;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;
import com.gmail.comcorecrew.comcore.server.connection.ServerTask;
import com.gmail.comcorecrew.comcore.server.entry.*;
import com.gmail.comcorecrew.comcore.server.id.*;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

/**
 * The reader thread which handles messages received from the server.
 */
public final class ServerReader extends ServerThread {
    /**
     * Create a ServerReader associated with a ServerConnection.
     *
     * @param connection the ServerConnection which will handle the tasks
     */
    public ServerReader(ServerConnection connection) {
        super(connection);
    }

    @Override
    protected void step() {
        ServerMsg message = connection.receiveMessage();
        if (message == null) {
            return;
        }

        ServerTask task = null;
        try {
            switch (message.kind) {
                case "REPLY": {
                    task = getTask();
                    if (task != null) {
                        task.handleResult(ServerResult.success(message.data));
                    }
                    break;
                }
                case "ERROR": {
                    task = getTask();
                    if (task != null) {
                        String errorMessage = message.data.get("message").getAsString();
                        task.handleResult(ServerResult.failure(errorMessage));
                    }
                    break;
                }
                case "setUser": {
                    UserInfo userData = UserInfo.fromJson(message.data);
                    connection.setUserInfo(userData);
                    break;
                }
                case "message": {
                    MessageEntry entry = MessageEntry.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onReceiveMessage(entry));
                    break;
                }
                case "messageUpdate": {
                    MessageID id = MessageID.fromJson(null, message.data);
                    String newContents = message.data.get("newContents").getAsString();
                    long timestamp = message.data.get("timestamp").getAsLong();
                    ServerConnector.sendNotification(listener ->
                            listener.onMessageUpdated(id, newContents, timestamp));
                    break;
                }
                case "task": {
                    TaskEntry entry = TaskEntry.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onTaskAdded(entry));
                    break;
                }
                case "taskUpdated": {
                    TaskID id = TaskID.fromJson(null, message.data);
                    boolean completed = message.data.get("completed").getAsBoolean();
                    ServerConnector.sendNotification(listener ->
                            listener.onTaskUpdated(id, completed));
                    break;
                }
                case "taskDeleted": {
                    TaskID id = TaskID.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onTaskDeleted(id));
                    break;
                }
                case "invite": {
                    GroupInviteEntry entry = GroupInviteEntry.fromJson(message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onInvitedToGroup(entry));
                    break;
                }
                case "roleChanged": {
                    GroupID group = new GroupID(message.data.get("group").getAsString());
                    GroupRole role = GroupRole.fromString(message.data.get("role").getAsString());
                    ServerConnector.sendNotification(listener ->
                            listener.onRoleChanged(group, role));
                    break;
                }
                case "mutedChanged": {
                    GroupID group = new GroupID(message.data.get("group").getAsString());
                    boolean muted = message.data.get("muted").getAsBoolean();
                    ServerConnector.sendNotification(listener ->
                            listener.onMuteChanged(group, muted));
                    break;
                }
                case "kicked": {
                    GroupID group = new GroupID(message.data.get("group").getAsString());
                    ServerConnector.sendNotification(listener ->
                        listener.onKicked(group));
                    break;
                }
                case "logout": {
                    ServerConnector.sendNotification(NotificationListener::onLoggedOut);
                    break;
                }
                default:
                    System.err.println("Unknown message kind: " + message.kind);
            }
        } catch (Exception e) {
            if (task != null) {
                // If there was a task reply, but the structure was invalid, send invalid response
                task.handleResult(ServerResult.invalidResponse());
            }
            e.printStackTrace();
        }
    }
}
package com.gmail.comcorecrew.comcore.server.connection.thread;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.LoginToken;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.gmail.comcorecrew.comcore.server.connection.ServerMsg;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;
import com.gmail.comcorecrew.comcore.server.connection.ServerTask;
import com.gmail.comcorecrew.comcore.server.entry.*;
import com.gmail.comcorecrew.comcore.server.id.*;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

import java.util.Map;

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
                case "message": {
                    MessageEntry entry = MessageEntry.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onReceiveMessage(entry));
                    break;
                }
                case "messageUpdated": {
                    MessageEntry entry = MessageEntry.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onMessageUpdated(entry));
                    break;
                }
                case "reaction": {
                    MessageID id = MessageID.fromJson(null, message.data);
                    Map<UserID, String> reactions = MessageEntry.parseReactions(message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onReactionUpdated(id, reactions));
                    break;
                }
                case "task": {
                    TaskEntry entry = TaskEntry.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onTaskAdded(entry));
                    break;
                }
                case "taskUpdated": {
                    TaskEntry entry = TaskEntry.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onTaskUpdated(entry));
                    break;
                }
                case "taskDeleted": {
                    TaskID id = TaskID.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onTaskDeleted(id));
                    break;
                }
                case "event": {
                    EventEntry entry = EventEntry.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onEventAdded(entry));
                    break;
                }
                case "eventApproved": {
                    EventID id = EventID.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onEventApproved(id));
                    break;
                }
                case "eventUpdated": {
                    EventEntry entry = EventEntry.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onEventUpdated(entry));
                    break;
                }
                case "eventDeleted": {
                    EventID id = EventID.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onEventDeleted(id));
                    break;
                }
                case "poll": {
                    PollEntry entry = PollEntry.fromJson(null, message.data);
                    ServerConnector.sendNotification(listener ->
                            listener.onPollAdded(entry));
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
                    String name = message.data.get("name").getAsString();
                    ServerConnector.sendNotification(listener ->
                        listener.onKicked(group, name));
                    break;
                }
                case "login": {
                    UserInfo userInfo = UserInfo.fromJson(message.data);
                    String tokenString = message.data.get("token").getAsString();
                    LoginToken token = new LoginToken(userInfo.id, tokenString);
                    connection.loggedIn(userInfo, token);
                    break;
                }
                case "logout": {
                    connection.loggedOut();
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
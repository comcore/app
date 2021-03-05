package com.gmail.comcorecrew.comcore.server.connection.thread;

import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.gmail.comcorecrew.comcore.server.connection.Message;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;
import com.gmail.comcorecrew.comcore.server.connection.Task;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;

/**
 * The reader thread which handles messages received from the server.
 */
public class ServerReader extends ServerThread {
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
        Message message = connection.receiveMessage();
        if (message == null) {
            return;
        }

        Task task = null;
        try {
            switch (message.kind) {
                case "REPLY":
                    task = getTask();
                    if (task != null) {
                        task.handleResult(ServerResult.success(message.data));
                    }
                    break;
                case "ERROR":
                    task = getTask();
                    if (task != null) {
                        String errorMessage = message.data.get("message").getAsString();
                        task.handleResult(ServerResult.failure(errorMessage));
                    }
                    break;
                case "message":
                    MessageEntry entry = MessageEntry.fromJson(null, message.data);
                    ServerConnector.foreachListener(listener -> {
                        listener.onReceiveMessage(entry);
                        return false;
                    });
                    break;
                case "logout":
                    ServerConnector.foreachListener(listener -> {
                        listener.onLoggedOut();
                        return false;
                    });
                    break;
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
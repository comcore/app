package com.gmail.comcorecrew.comcore.server.connection.thread;

import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;

/**
 * The writer thread which initiates tasks by sending them to the server.
 */
public class ServerWriter extends ServerThread {
    /**
     * Create a ServerWriter associated with a ServerConnection.
     *
     * @param connection the ServerConnection which will handle the tasks
     */
    public ServerWriter(ServerConnection connection) {
        super(connection);
    }

    @Override
    protected void step() {
        connection.startTask(getTask());
    }
}
package com.gmail.comcorecrew.comcore.server.connection;

import android.os.Handler;
import android.os.Looper;

import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.google.gson.JsonObject;

/**
 * Represents a Task which can be executed by a ServerConnection.
 */
public final class Task implements ResultHandler<JsonObject> {
    /**
     * The message to send when starting the task.
     */
    public final Message message;

    /**
     * A handler for the result returned by the server (may be null).
     */
    public final ResultHandler<JsonObject> handler;

    /**
     * Create a task from a message to initiate the task and a handler for when the task completes.
     *
     * @param message the message to send
     * @param handler the handler for the response of the server
     */
    public Task(Message message, ResultHandler<JsonObject> handler) {
        if (message == null) {
            throw new IllegalArgumentException("task message cannot be null");
        }

        this.message = message;
        this.handler = handler;
    }

    @Override
    public void handleResult(ServerResult<JsonObject> result) {
        // Handle the result on the main thread instead of the server thread
        if (handler != null) {
            new Handler(Looper.getMainLooper()).post(() -> handler.handleResult(result));
        }
    }
}
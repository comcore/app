package com.gmail.comcorecrew.comcore.server;

/**
 * Represents a callback for asynchronous ServerConnector methods.
 *
 * @param <T> the type of result expected from the server request
 */
public interface ResultHandler<T> {
    /**
     * Do something with the result of the server request.
     *
     * @param result the result of the server request
     */
    void handleResult(ServerResult<T> result);
}
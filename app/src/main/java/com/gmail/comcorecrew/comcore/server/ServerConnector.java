package com.gmail.comcorecrew.comcore.server;

/**
 * Singleton class representing a connection with the server.
 */
public abstract class ServerConnector {
    private static ServerConnector INSTANCE;

    /**
     * Set the instance which will be used for all server requests. This could be used to set a
     * MockConnector for testing purposes before making a request.
     *
     * @param connector the ServerConnector instance to use for requests
     */
    public static void setInstance(ServerConnector connector) {
        if (INSTANCE != null) {
            INSTANCE.stop();
        }
        INSTANCE = connector;
    }

    /**
     * Get the ServerConnector instance which should be used for server requests.
     *
     * @return the ServerConnector instance to use for requests
     */
    private static ServerConnector getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerConnectorImpl();
        }
        return INSTANCE;
    }

    /**
     * Close the connection to the server.
     */
    protected void stop() {}

    /**
     * Send a message to the server and wait for a response synchronously.
     *
     * @param message the message to send to the server
     * @return a ServerResult containing the response of the server
     */
    protected abstract ServerResult<String> send(String message);

    /**
     * Send a message to the server and handle the result asynchronously when it arrives.
     * If the handler is null, the server response will be ignored.
     *
     * @param message the message to send to the server
     * @param handler the handler for the response of the server
     */
    protected void sendAsync(String message, ResultHandler<String> handler) {
        ServerResult<String> result = send(message);
        if (handler != null) {
            handler.handleResult(result);
        }
    }

    // TODO add methods for various types of requests
}
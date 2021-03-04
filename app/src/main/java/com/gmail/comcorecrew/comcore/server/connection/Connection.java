package com.gmail.comcorecrew.comcore.server.connection;

import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.LoginStatus;
import com.google.gson.JsonObject;

public interface Connection {
    /**
     * Close the connection to the server.
     */
    void stop();

    /**
     * Request the server to authenticate the user. This should be called before making any requests
     * from the server. If this method is not called, all requests will likely fail.
     *
     * @param email         the user's email address
     * @param pass          the user's password
     * @param createAccount true if creating an account, false if logging into an existing account
     * @param handler       the handler for the response of the server
     */
    void authenticate(String email, String pass, boolean createAccount,
                      ResultHandler<LoginStatus> handler);

    /**
     * Send a message to the server and handle the result asynchronously when it arrives.
     * If the handler is null, the server response will be ignored.
     *
     * @param message  the message to send to the server
     * @param handler  the handler for the response of the server
     * @param function a function to convert the raw response to the proper type
     */
    <T> void send(Message message, ResultHandler<T> handler, Function<JsonObject, T> function);
}
package com.gmail.comcorecrew.comcore.server.connection;

import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.LoginStatus;
import com.google.gson.JsonObject;

/**
 * Interface for classes which manage connections to a server.
 */
public interface Connection {
    /**
     * Close the connection to the server permanently (future requests will always fail). This
     * method cannot be called from the main thread.
     */
    void stop();

    /**
     * Log out if logged in. It will be necessary to call authenticate() again.
     */
    void logout();

    /**
     * Request the server to authenticate the user. This should be called before making any requests
     * from the server. If this method is not called, all requests will likely fail.
     *
     * If createAccount is true, an account will be created for the user. LoginStatus.ENTER_CODE
     * will be returned if successful, otherwise LoginStatus.ALREADY_EXISTS will be returned if the
     * email address is already in use.
     *
     * If requestReset() and enterCode() have been successfully called, the user's password will be
     * reset to the provided password and they will be signed in.
     *
     * Otherwise, the password will be checked against the account on the server.
     *
     * @param email         the user's email address
     * @param pass          the user's password
     * @param createAccount true if creating an account, false if logging into an existing account
     * @param handler       the handler for the response of the server
     * @see LoginStatus
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
package com.gmail.comcorecrew.comcore.server.connection;

import com.gmail.comcorecrew.comcore.server.LoginToken;
import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;
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
     * Get the information of the currently logged in user.
     *
     * @return the user data or null if there is no logged in user
     */
    UserInfo getUserInfo();

    /**
     * Set the login information to use for connecting to the server. Note that this doesn't
     * actually start the login process, it just stores the credentials in case the client is
     * disconnected and needs to try to log in automatically.
     *
     * @param email the user's email address
     * @param pass  the user's password
     */
    void setInformation(String email, String pass);

    /**
     * Log into the server using a token.
     */
    void login(LoginToken token);

    /**
     * Send a message to the server and handle the result asynchronously when it arrives.
     * If the handler is null, the server response will be ignored.
     *
     * @param message  the message to send to the server
     * @param handler  the handler for the response of the server
     * @param function a function to convert the raw response to the proper type
     */
    <T> void send(ServerMsg message, ResultHandler<T> handler, Function<JsonObject, T> function);
}
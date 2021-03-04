package com.gmail.comcorecrew.comcore.server;

/**
 * Represents the result of a login request.
 */
public enum LoginStatus {
    /**
     * The login was successful and the user is authenticated to use the app.
     */
    SUCCESS,

    /**
     * The login was successful, but the user must enter a code sent to their email address.
     */
    ENTER_CODE,

    /**
     * The login failed because the account did not exist.
     */
    DOES_NOT_EXIST,

    /**
     * The login failed because the user entered the wrong password.
     */
    INVALID_PASSWORD,
}
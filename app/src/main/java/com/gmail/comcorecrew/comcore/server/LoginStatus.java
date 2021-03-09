package com.gmail.comcorecrew.comcore.server;

import com.google.gson.JsonObject;

/**
 * Represents the result of a login request.
 */
public enum LoginStatus {
    /**
     * The login was successful and the user is authenticated to use the app.
     */
    SUCCESS(true),

    /**
     * The login was successful, but the user must enter a code sent to their email address. The
     * code can be sent with ServerConnector.enterCode().
     */
    ENTER_CODE(true),

    /**
     * The login failed because the account does not exist.
     */
    DOES_NOT_EXIST(false),

    /**
     * The login failed because the user entered the wrong password.
     */
    INVALID_PASSWORD(false);

    /**
     * True if the login information (email and password) were valid.
     */
    public final boolean isValid;

    LoginStatus(boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * Create a LoginStatus from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the LoginStatus
     */
    public static LoginStatus fromJson(JsonObject json) {
        String status = json.get("status").getAsString();
        switch (status) {
            case "SUCCESS":
                return LoginStatus.SUCCESS;
            case "ENTER_CODE":
                return LoginStatus.ENTER_CODE;
            case "DOES_NOT_EXIST":
                return LoginStatus.DOES_NOT_EXIST;
            case "INVALID_PASSWORD":
                return LoginStatus.INVALID_PASSWORD;
            default:
                throw new IllegalArgumentException("invalid LoginStatus: " + status);
        }
    }
}
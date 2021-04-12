package com.gmail.comcorecrew.comcore.server;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.util.Objects;

/**
 * Represents a login token returned by the server which can be used to log in again on this device
 * in the future without needed an email and password.
 */
public class LoginToken {
    /**
     * The UserID of the user to log in.
     */
    public final UserID user;

    /**
     * The token string sent by the server.
     */
    public final String token;

    /**
     * Create a LoginToken from a token string.
     *
     * @param user  the ID of the user
     * @param token the token string
     */
    public LoginToken(UserID user, String token) {
        if (user == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        } else if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token cannot be null or empty");
        }

        this.user = user;
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginToken that = (LoginToken) o;
        return user.equals(that.user) &&
                token.equals(that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, token);
    }

    @Override
    @NonNull
    public String toString() {
        return token;
    }
}
package com.gmail.comcorecrew.comcore.server;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents a login token returned by the server which can be used to log in again on this device
 * in the future without needed an email and password.
 */
public class LoginToken {
    /**
     * The token string sent by the server.
     */
    public final String token;

    /**
     * Create a LoginToken from a token string.
     *
     * @param token the token string
     */
    public LoginToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("token cannot be null or empty");
        }

        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginToken that = (LoginToken) o;
        return token.equals(that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    @NonNull
    public String toString() {
        return token;
    }
}
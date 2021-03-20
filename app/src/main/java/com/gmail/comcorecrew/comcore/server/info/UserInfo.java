package com.gmail.comcorecrew.comcore.server.info;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents user info returned by the server.
 */
public final class UserInfo {
    /**
     * The user's identifier.
     */
    public final UserID id;

    /**
     * The user's name.
     */
    public final String name;

    /**
     * Create a UserInfo from a UserID and a name.
     *
     * @param id   the UserID of the user
     * @param name the name of the user
     */
    public UserInfo(UserID id, String name) {
        if (id == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("user name cannot be null or empty");
        }

        this.id = id;
        this.name = name;
    }

    /**
     * Parse a UserInfo from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the UserInfo
     */
    public static UserInfo fromJson(JsonObject json) {
        UserID id = new UserID(json.get("id").getAsString());
        String name = json.get("name").getAsString();
        return new UserInfo(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userEntry = (UserInfo) o;
        return id.equals(userEntry.id) &&
                name.equals(userEntry.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
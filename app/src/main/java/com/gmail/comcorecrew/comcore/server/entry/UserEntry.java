package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of user data returned by the server.
 */
public final class UserEntry {
    /**
     * The user's identifier.
     */
    public final UserID id;

    /**
     * The user's name.
     */
    public final String name;

    /**
     * Create a UserEntry from a UserID and a name
     *
     * @param id   the UserID of the group
     * @param name the name of the group
     */
    public UserEntry(UserID id, String name) {
        if (id == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("user name cannot be null or empty");
        }

        this.id = id;
        this.name = name;
    }

    /**
     * Parse a UserEntry from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the UserEntry
     */
    public static UserEntry fromJson(JsonObject json) {
        UserID id = new UserID(json.get("id").getAsString());
        String name = json.get("name").getAsString();
        return new UserEntry(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntry userEntry = (UserEntry) o;
        return id.equals(userEntry.id) &&
                name.equals(userEntry.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
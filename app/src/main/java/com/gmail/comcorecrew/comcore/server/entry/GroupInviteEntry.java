package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of group invite data returned by the server.
 */
public final class GroupInviteEntry {
    /**
     * The group's identifier.
     */
    public final GroupID id;

    /**
     * The group's name.
     */
    public final String name;

    /**
     * The name of the person who invited the user.
     */
    public final String inviter;

    /**
     * Create a GroupInviteEntry from a GroupID, name, and inviter.
     */
    public GroupInviteEntry(GroupID id, String name, String inviter) {
        if (id == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("group name cannot be null or empty");
        } else if (inviter == null || inviter.isEmpty()) {
            throw new IllegalArgumentException("inviter name cannot be null or empty");
        }

        this.id = id;
        this.name = name;
        this.inviter = inviter;
    }

    /**
     * Create a GroupInviteEntry from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the GroupInviteEntry
     */
    public static GroupInviteEntry fromJson(JsonObject json) {
        GroupID id = new GroupID(json.get("id").getAsString());
        String name = json.get("name").getAsString();
        String inviter = json.get("inviter").getAsString();
        return new GroupInviteEntry(id, name, inviter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupInviteEntry that = (GroupInviteEntry) o;
        return id.equals(that.id) &&
                name.equals(that.name) &&
                inviter.equals(that.inviter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, inviter);
    }
}
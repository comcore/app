package com.gmail.comcorecrew.comcore.server.entry;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of group data returned by the server.
 */
public final class GroupEntry {
    /**
     * The group's identifier.
     */
    public final GroupID id;

    /**
     * The group's name.
     */
    public final String name;

    /**
     * The user's role in the group.
     */
    public final GroupRole role;

    /**
     * True if the user is muted, false otherwise.
     */
    public final boolean muted;

    /**
     * Create a GroupEntry from a GroupID, name, role, and muted status.
     *
     * @param id    the GroupID of the group
     * @param name  the name of the group
     * @param role  the role of the user
     * @param muted the user's muted status
     */
    public GroupEntry(GroupID id, String name, GroupRole role, boolean muted) {
        if (id == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("group name cannot be null or empty");
        } else if (role == null) {
            throw new IllegalArgumentException("GroupRole cannot be null");
        } else if (muted && role == GroupRole.OWNER) {
            throw new IllegalArgumentException("group owner cannot be muted");
        }

        this.id = id;
        this.name = name;
        this.role = role;
        this.muted = muted;
    }

    /**
     * Create a GroupEntry from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the GroupEntry
     */
    public static GroupEntry fromJson(JsonObject json) {
        GroupID id = new GroupID(json.get("id").getAsString());

        /** Currently, the json does not return name from the server
         * Until this bug is fixed, the group's name is set as the id
         */
        //String name = json.get("name").getAsString();
        String name = id.toString();


        GroupRole role = GroupRole.fromString(json.get("role").getAsString());
        boolean muted = json.get("muted").getAsBoolean();
        return new GroupEntry(id, name, role, muted);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupEntry that = (GroupEntry) o;
        return muted == that.muted &&
                id.equals(that.id) &&
                name.equals(that.name) &&
                role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, role, muted);
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
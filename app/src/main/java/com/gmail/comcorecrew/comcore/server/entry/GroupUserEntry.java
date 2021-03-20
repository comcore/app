package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of user data returned by the server for a group.
 */
public final class GroupUserEntry {
    /**
     * The user's identifier.
     */
    public final UserID id;

    /**
     * The user's role in the group.
     */
    public final GroupRole role;

    /**
     * True if the user is muted, false otherwise.
     */
    public final boolean muted;

    /**
     * Create a GroupUserEntry from a GroupID, name, role, and muted status.
     *
     * @param id    the GroupID of the group
     * @param name  the name of the group
     * @param role  the role of the user
     * @param muted the user's muted status
     */
    public GroupUserEntry(UserID id, GroupRole role, boolean muted) {
        if (id == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        } else if (role == null) {
            throw new IllegalArgumentException("GroupRole cannot be null");
        } else if (muted && role == GroupRole.OWNER) {
            throw new IllegalArgumentException("group owner cannot be muted");
        }

        this.id = id;
        this.role = role;
        this.muted = muted;
    }

    /**
     * Create a GroupUserEntry from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the GroupUserEntry
     */
    public static GroupUserEntry fromJson(JsonObject json) {
        UserID id = new UserID(json.get("id").getAsString());
        GroupRole role = GroupRole.fromString(json.get("role").getAsString());
        boolean muted = json.get("muted").getAsBoolean();
        return new GroupUserEntry(id, role, muted);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupUserEntry that = (GroupUserEntry) o;
        return muted == that.muted &&
                id.equals(that.id) &&
                role == that.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, role, muted);
    }
}
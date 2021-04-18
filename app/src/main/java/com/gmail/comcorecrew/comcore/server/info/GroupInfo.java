package com.gmail.comcorecrew.comcore.server.info;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents group info returned by the server.
 */
public final class GroupInfo {
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
     * Whether users need moderator approval to create calendar events.
     */
    public final boolean requireApproval;

    /**
     * Timestamp for when the group's info was last updated.
     */
    public final long lastUpdate;

    /**
     * Create a GroupInfo from a GroupID, name, role, and muted status.
     *
     * @param id         the GroupID of the group
     * @param name       the name of the group
     * @param role       the role of the user
     * @param muted      the user's muted status
     * @param lastUpdate when the group info was last updated
     */
    public GroupInfo(GroupID id, String name, GroupRole role, boolean muted,
                     boolean requireApproval, long lastUpdate) {
        if (id == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("group name cannot be null or empty");
        } else if (role == null) {
            throw new IllegalArgumentException("GroupRole cannot be null");
        } else if (muted && role == GroupRole.OWNER) {
            throw new IllegalArgumentException("group owner cannot be muted");
        } else if (lastUpdate < 0) {
            throw new IllegalArgumentException("last update time cannot be negative");
        }

        this.id = id;
        this.name = name;
        this.role = role;
        this.muted = muted;
        this.requireApproval = requireApproval;
        this.lastUpdate = lastUpdate;
    }

    /**
     * Create a GroupInfo from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the GroupInfo
     */
    public static GroupInfo fromJson(JsonObject json) {
        GroupID id = new GroupID(json.get("id").getAsString());
        String name = json.get("name").getAsString();
        GroupRole role = GroupRole.fromString(json.get("role").getAsString());
        boolean muted = json.get("muted").getAsBoolean();
        boolean requireApproval = json.get("requireApproval").getAsBoolean();
        JsonElement lastUpdateJson = json.get("lastUpdate");
        long lastUpdate = lastUpdateJson == null ? 0 : lastUpdateJson.getAsLong();
        return new GroupInfo(id, name, role, muted, requireApproval, lastUpdate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupInfo that = (GroupInfo) o;
        return muted == that.muted &&
                id.equals(that.id) &&
                name.equals(that.name) &&
                role == that.role &&
                lastUpdate == that.lastUpdate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, role, muted, lastUpdate);
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
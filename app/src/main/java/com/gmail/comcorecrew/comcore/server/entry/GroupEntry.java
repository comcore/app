package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.GroupID;

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
     * Create a GroupEntry from a GroupID and a name
     *
     * @param id   the GroupID of the group
     * @param name the name of the group
     */
    public GroupEntry(GroupID id, String name) {
        if (id == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("group name cannot be null or empty");
        }

        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupEntry that = (GroupEntry) o;
        return id.equals(that.id) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.GroupID;

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
     * @param id   the GroupID of the group
     * @param name the name of the group
     */
    public GroupEntry(GroupID id, String name) {
        this.id = id;
        this.name = name;
    }
}
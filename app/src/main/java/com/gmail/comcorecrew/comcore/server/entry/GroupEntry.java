package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.GroupID;

public final class GroupEntry {
    public final GroupID id;
    public final String name;

    public GroupEntry(GroupID id, String name) {
        this.id = id;
        this.name = name;
    }
}
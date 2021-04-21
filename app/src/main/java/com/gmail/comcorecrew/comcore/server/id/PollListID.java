package com.gmail.comcorecrew.comcore.server.id;

/**
 * Represents a unique identifier for a poll list in a group.
 */
public final class PollListID extends ModuleID {
    /**
     * Create a PollListID from a parent group and an ID string.
     *
     * @param group the parent group
     * @param id    the ID string
     */
    public PollListID(GroupID group, String id) {
        super(group, id);
    }

    @Override
    public String getType() {
        return "poll";
    }
}
package com.gmail.comcorecrew.comcore.server.id;

/**
 * Represents a unique identifier for a calendar in a group.
 */
public final class CalendarID extends ModuleID {
    /**
     * Create a CalendarID from a parent group and an ID string.
     *
     * @param group the parent group
     * @param id    the ID string
     */
    public CalendarID(GroupID group, String id) {
        super(group, id);
    }

    @Override
    public String getType() {
        return "cal";
    }
}
package com.gmail.comcorecrew.comcore.server.id;

/**
 * Represents a unique identifier for a task list in a group.
 */
public final class TaskListID extends ModuleID {
    /**
     * Create a TaskList from a parent group and an ID string.
     *
     * @param group the parent group
     * @param id    the ID string
     */
    public TaskListID(GroupID group, String id) {
        super(group, id);
    }

    @Override
    public String getType() {
        return "task";
    }
}
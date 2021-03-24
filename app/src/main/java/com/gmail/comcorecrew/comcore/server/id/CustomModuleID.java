package com.gmail.comcorecrew.comcore.server.id;

/**
 * Represents a unique identifier for a custom module in a group. Can be converted to other types of
 * modules to access their functionality.
 */
public final class CustomModuleID extends ModuleID {
    /**
     * The type string of the module.
     */
    public final String type;

    /**
     * Create a CustomModuleID from a parent group, an ID string, and a type string.
     *
     * @param group the parent group
     * @param id    the ID string
     * @param type  the type string of the module
     */
    public CustomModuleID(GroupID group, String id, String type) {
        super(group, id);

        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("type string cannot be empty");
        } else if (isKnownType(type)) {
            throw new IllegalArgumentException("cannot create CustomModuleID with type " + type);
        }

        this.type = type;
    }

    /**
     * Treat this custom module as if it were a chat.
     *
     * @return a ChatID associated with this module
     */
    public ChatID asChat() {
        return new ChatID(group, id);
    }

    /**
     * Treat this custom module as if it were a task list.
     *
     * @return a TaskListID associated with this module
     */
    public TaskListID asTaskList() {
        return new TaskListID(group, id);
    }

    @Override
    public String getType() {
        return type;
    }
}
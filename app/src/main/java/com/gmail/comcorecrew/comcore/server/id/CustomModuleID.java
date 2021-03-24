package com.gmail.comcorecrew.comcore.server.id;

/**
 * Represents a unique identifier for a custom module in a group.
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
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }
}
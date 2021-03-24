package com.gmail.comcorecrew.comcore.server.id;

/**
 * Represents a unique identifier for a chat in a group.
 */
public final class ChatID extends ModuleID {
    /**
     * Create a ChatID from a parent group and an ID string.
     *
     * @param group the parent group
     * @param id    the ID string
     */
    public ChatID(GroupID group, String id) {
        super(group, id);
    }

    @Override
    public String getType() {
        return "chat";
    }
}
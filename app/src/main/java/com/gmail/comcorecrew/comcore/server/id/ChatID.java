package com.gmail.comcorecrew.comcore.server.id;

import java.util.Objects;

/**
 * Represents a unique identifier for a chat in a group.
 */
public class ChatID extends AbstractID {
    /**
     * The group that this chat belongs to.
     */
    public final GroupID group;

    /**
     * Create a ChatID from a parent group and an ID string.
     *
     * @param group the parent group
     * @param id    the ID string
     */
    public ChatID(GroupID group, String id) {
        super(id);

        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        }

        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChatID chatID = (ChatID) o;
        return group.equals(chatID.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), group);
    }
}
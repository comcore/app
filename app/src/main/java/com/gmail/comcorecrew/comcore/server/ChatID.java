package com.gmail.comcorecrew.comcore.server;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a unique identifier for a chat in a group.
 */
public class ChatID extends AbstractID {
    /**
     * The group that this chat belongs to.
     */
    public final GroupID group;

    /**
     * Create a ChatID from a UUID.
     *
     * @param group the parent group
     * @param uuid  the UUID
     */
    public ChatID(GroupID group, UUID uuid) {
        super(uuid);
        this.group = group;
    }

    /**
     * Create a ChatID from a UUID represented as a String.
     *
     * @param group the parent group
     * @param uuid the UUID String
     * @throws IllegalArgumentException if the UUID String is invalid
     */
    public ChatID(GroupID group, String uuid) {
        super(uuid);
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
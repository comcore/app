package com.gmail.comcorecrew.comcore.server.id;

import java.util.UUID;

/**
 * Represents a unique identifier for a group.
 */
public class GroupID extends AbstractID {
    /**
     * Create a GroupID from a UUID.
     *
     * @param uuid the UUID
     */
    public GroupID(UUID uuid) {
        super(uuid);
    }

    /**
     * Create a GroupID from a UUID represented as a String.
     *
     * @param uuid the UUID String
     * @throws IllegalArgumentException if the UUID String is invalid
     */
    public GroupID(String uuid) {
        super(uuid);
    }
}
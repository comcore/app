package com.gmail.comcorecrew.comcore.server.id;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a unique identifier for a type of item.
 */
public abstract class AbstractID {
    /**
     * The UUID corresponding to this item.
     */
    public final UUID uuid;

    /**
     * Create an AbstractID from a UUID.
     *
     * @param uuid the UUID
     */
    public AbstractID(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Create an AbstractID from a UUID represented as a String.
     *
     * @param uuid the UUID String
     * @throws IllegalArgumentException if the UUID String is invalid
     */
    public AbstractID(String uuid) {
        this(UUID.fromString(uuid));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractID that = (AbstractID) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
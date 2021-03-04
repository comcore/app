package com.gmail.comcorecrew.comcore.server.id;

import java.util.Objects;

/**
 * Represents a unique identifier for a type of item.
 */
public abstract class AbstractID {
    /**
     * The ID string corresponding to this item.
     */
    public final String id;

    /**
     * Create an AbstractID from an ID string.
     *
     * @param id the ID string
     */
    public AbstractID(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }

        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractID that = (AbstractID) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
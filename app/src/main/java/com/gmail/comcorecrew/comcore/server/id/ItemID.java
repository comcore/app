package com.gmail.comcorecrew.comcore.server.id;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents a unique identifier for a type of item.
 */
public abstract class ItemID {
    /**
     * The ID string corresponding to this item.
     */
    public final String id;

    /**
     * Create an ItemID from an ID string.
     *
     * @param id the ID string
     */
    public ItemID(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }

        this.id = id;
    }

    /**
     * Convert this ItemID to a JSON object.
     *
     * @return the JsonObject
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemID that = (ItemID) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    @NonNull
    public String toString() {
        return id;
    }
}
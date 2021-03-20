package com.gmail.comcorecrew.comcore.server.info;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.server.id.ModuleID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents module info returned by the server.
 *
 * @param <T> the specific type of ModuleID
 */
public final class ModuleInfo<T extends ModuleID> {
    /**
     * The module's identifier.
     */
    public final T id;

    /**
     * The module's name.
     */
    public final String name;

    /**
     * Create a ModuleInfo from a ModuleID and a name.
     *
     * @param id   the ModuleID of the module
     * @param name the name of the module
     */
    public ModuleInfo(T id, String name) {
        if (id == null) {
            throw new IllegalArgumentException("ModuleID cannot be null");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("module name cannot be null or empty");
        }

        this.id = id;
        this.name = name;
    }

    /**
     * Parse a ModuleInfo from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the ModuleInfo
     */
    public static ModuleInfo<?> fromJson(JsonObject json) {
        ModuleID id = ModuleID.fromJson(null, json);
        String name = json.get("name").getAsString();
        return new ModuleInfo<>(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleInfo<?> moduleEntry = (ModuleInfo<?>) o;
        return id.equals(moduleEntry.id) &&
                name.equals(moduleEntry.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
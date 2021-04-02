package com.gmail.comcorecrew.comcore.server.info;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents module info returned by the server.
 */
public final class ModuleInfo {
    /**
     * The module's identifier.
     */
    public final ModuleID id;

    /**
     * The module's name.
     */
    public final String name;

    /**
     * Timestamp for the last cache-clearing action in this module. A cache clearing action is one
     * that invalidates cached data. In the case of chats, sending a message is not cache-clearing
     * since it can be added to the end of the cache. However, editing/deleting an old message is
     * cache-clearing since the previously cached message data becomes invalid. Therefore, if this
     * timestamp is more recent than the last time the cache was updated, then the cache should be
     * cleared and the data reloaded.
     */
    public final long cacheClearTimestamp;

    /**
     * Whether the module is enabled and should be shown to the user.
     */
    public final boolean enabled;

    /**
     * Create a ModuleInfo from a ModuleID and a name.
     *
     * @param id      the ModuleID of the module
     * @param name    the name of the module
     * @param cacheClearTimestamp when the last cache-clearing action occurred
     * @param enabled whether the module is enabled
     */
    public ModuleInfo(ModuleID id, String name, long cacheClearTimestamp, boolean enabled) {
        if (id == null) {
            throw new IllegalArgumentException("ModuleID cannot be null");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("module name cannot be null or empty");
        } else if (cacheClearTimestamp < 0) {
            throw new IllegalArgumentException("cache clear time cannot be negative");
        }

        this.id = id;
        this.name = name;
        this.cacheClearTimestamp = cacheClearTimestamp;
        this.enabled = enabled;
    }

    /**
     * Parse a ModuleInfo from a JsonObject.
     *
     * @param group the group which the module was retrieved from or null
     * @param json the data sent by the server
     * @return the ModuleInfo
     */
    public static ModuleInfo fromJson(GroupID group, JsonObject json) {
        ModuleID id = ModuleID.fromJson(group, json);
        String name = json.get("name").getAsString();
        JsonElement lastUpdateJson = json.get("lastUpdate");
        long cacheClearTimestamp = lastUpdateJson == null ? 0 : lastUpdateJson.getAsLong();
        boolean enabled = json.get("enabled").getAsBoolean();
        return new ModuleInfo(id, name, cacheClearTimestamp, enabled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleInfo moduleEntry = (ModuleInfo) o;
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
package com.gmail.comcorecrew.comcore.server.id;

import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents a unique identifier for a module in a group.
 */
public abstract class ModuleID extends ItemID {
    /**
     * The group that this module belongs to.
     */
    public final GroupID group;

    /**
     * Create a ModuleID from a parent group and an ID string.
     *
     * @param group the parent group
     * @param id    the ID string
     */
    public ModuleID(GroupID group, String id) {
        super(id);

        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        }

        this.group = group;
    }

    /**
     * Parse a ModuleID from a JsonObject. If specified, the given GroupID is used. Otherwise,
     * it is taken from the JSON data.
     *
     * @param group the group which the module was retrieved from or null
     * @param json  the data sent by the server
     * @return the ModuleID
     */
    public static ModuleID fromJson(GroupID group, JsonObject json) {
        if (group == null) {
            group = new GroupID(json.get("group").getAsString());
        }
        String id = json.get("id").getAsString();
        String kind = json.get("kind").getAsString();
        switch (kind) {
            case "chat":
                return new ChatID(group, id);
            default:
                throw new IllegalArgumentException("invalid module kind: " + kind);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ModuleID moduleID = (ModuleID) o;
        return group.equals(moduleID.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), group);
    }
}
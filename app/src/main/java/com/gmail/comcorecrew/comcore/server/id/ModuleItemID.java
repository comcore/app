package com.gmail.comcorecrew.comcore.server.id;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Represents a sequential identifier for an item in a module.
 *
 * @param <M> the type of the module ID
 */
public class ModuleItemID<M extends ModuleID> {
    /**
     * The maximum value of a ModuleItemID (2^53). This is because it is the largest integer such
     * that all smaller integers can be stored in a double-precision floating point number.
     */
    public static final long MAX_ID = 0x20_0000_0000_0000L;

    /**
     * The parent module of this item.
     */
    public final M module;

    /**
     * The numeric ID corresponding to the item.
     */
    public final long id;

    /**
     * Create a ModuleItemID from a parent module and a numeric ID.
     *
     * @param module the parent module
     * @param id     the numeric ID
     */
    public ModuleItemID(M module, long id) {
        if (module == null) {
            throw new IllegalArgumentException("ModuleID cannot be null");
        } else if (id < 1 || id >= MAX_ID) {
            throw new IllegalArgumentException("module item ID must be between 1 and 2^53");
        }

        this.module = module;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleItemID<?> that = (ModuleItemID<?>) o;
        return id == that.id &&
                module.equals(that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, id);
    }

    @Override
    @NonNull
    public String toString() {
        return Long.toString(id);
    }
}

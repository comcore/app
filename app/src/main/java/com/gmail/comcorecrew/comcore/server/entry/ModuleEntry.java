package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.notifications.ScheduledNotification;
import com.gmail.comcorecrew.comcore.server.id.ModuleID;
import com.gmail.comcorecrew.comcore.server.id.ModuleItemID;

/**
 * Represents an abstract entry of data returned by the server.
 *
 * @param <M> the type of the module ID
 * @param <I> the type of the item ID
 */
public abstract class ModuleEntry<M extends ModuleID, I extends ModuleItemID<M>> {
    /**
     * The item's identifier.
     */
    public final I id;

    /**
     * Create a ModuleEntry from an identifier.
     *
     * @param id the identifier of the item
     */
    public ModuleEntry(I id) {
        if (id == null) {
            throw new IllegalArgumentException("ModuleItemID cannot be null");
        }

        this.id = id;
    }

    /**
     * Try to create a scheduled notification for this item.
     *
     * @return a notification to schedule (or null)
     */
    public abstract ScheduledNotification getScheduledNotification();
}
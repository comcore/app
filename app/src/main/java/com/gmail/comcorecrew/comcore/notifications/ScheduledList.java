package com.gmail.comcorecrew.comcore.notifications;

import com.gmail.comcorecrew.comcore.server.entry.ModuleEntry;
import com.gmail.comcorecrew.comcore.server.id.ModuleItemID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Wrapper around an ArrayList which also automatically schedules notifications for upcoming
 * deadlines using the NotificationScheduler.
 *
 * @param <I> the item ID for an item
 * @param <T> the entry type for an item
 */
public class ScheduledList<I extends ModuleItemID<?>, T extends ModuleEntry<?, I>> {
    private final ArrayList<T> items = new ArrayList<>();

    /**
     * Get the entry at an index.
     *
     * @param index the index
     * @return the entry
     */
    public T get(int index) {
        return items.get(index);
    }

    /**
     * Find the index for an item ID.
     *
     * @param id the item ID
     * @return the index of the corresponding entry, or -1 if it wasn't found
     */
    public int indexOf(I id) {
        for (int i = 0, s = items.size(); i < s; i++) {
            if (id.equals(items.get(i).id)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get an entry by its item ID.
     *
     * @param id the item ID
     * @return the entry, or null if it wasn't found
     */
    public T get(I id) {
        int index = indexOf(id);
        if (index == -1) {
            return null;
        }

        return items.get(index);
    }

    /**
     * Get the number of entries in the list.
     *
     * @return the number of entries in the list
     */
    public int size() {
        return items.size();
    }

    /**
     * Check if the list is empty.
     *
     * @return true if the list is empty, false otherwise
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Add a new entry to the list.
     *
     * @param item the entry to add
     */
    public void add(T item) {
        NotificationScheduler.add(item);
        items.add(item);
    }

    /**
     * Update the entry at an index.
     *
     * @param index the index to update
     * @param item  the new entry
     */
    public void set(int index, T item) {
        T old = items.set(index, item);
        if (!old.equals(item)) {
            NotificationScheduler.remove(old.id);
            NotificationScheduler.add(item);
        }
    }

    /**
     * Update an entry, replacing any previous entry with the same ID.
     *
     * @param item the new entry
     */
    public void update(T item) {
        int index = indexOf(item.id);
        if (index == -1) {
            add(item);
            return;
        }

        set(index, item);
    }

    /**
     * Remove the entry at the given index.
     *
     * @param index the index to remove the entry from
     */
    public void remove(int index) {
        NotificationScheduler.remove(items.remove(index).id);
    }

    /**
     * Remove the entry with the given item ID.
     *
     * @param id the item ID
     * @return true if the item existed, false otherwise
     */
    public boolean remove(I id) {
        int index = indexOf(id);
        if (index == -1) {
            return false;
        }

        remove(index);
        return true;
    }

    /**
     * Set the entries in the list. This will automatically find any differences between the two
     * lists and maintain the notifications without doing any unnecessary work.
     *
     * @param items the new entries
     */
    public void setEntries(List<T> items) {
        HashSet<ModuleItemID<?>> toDelete = new HashSet<>();
        for (T item : this.items) {
            toDelete.add(item.id);
        }

        for (T item : items) {
            toDelete.remove(item.id);
            NotificationScheduler.add(item);
        }

        for (ModuleItemID<?> item : toDelete) {
            NotificationScheduler.remove(item);
        }

        this.items.clear();
        this.items.addAll(items);
    }

    /**
     * Get the entries in the list. The resulting list is immutable.
     *
     * @return the entries in the lsit
     */
    public List<T> getEntries() {
        return Collections.unmodifiableList(items);
    }
}
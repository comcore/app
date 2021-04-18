package com.gmail.comcorecrew.comcore.notifications;

import com.gmail.comcorecrew.comcore.server.entry.ModuleEntry;
import com.gmail.comcorecrew.comcore.server.id.ModuleItemID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class ScheduledList<I extends ModuleItemID<?>, T extends ModuleEntry<?, I>> {
    private final ArrayList<T> items = new ArrayList<>();

    public synchronized T get(int index) {
        return items.get(index);
    }

    public int indexOf(I id) {
        for (int i = 0, s = items.size(); i < s; i++) {
            if (id.equals(items.get(i).id)) {
                return i;
            }
        }

        return -1;
    }

    public synchronized T get(I id) {
        int index = indexOf(id);
        if (index == -1) {
            return null;
        }

        return items.get(index);
    }

    public synchronized int size() {
        return items.size();
    }

    public synchronized void add(T item) {
        NotificationScheduler.add(item);
        items.add(item);
    }

    public synchronized void set(int index, T item) {
        T old = items.set(index, item);
        if (!old.equals(item)) {
            NotificationScheduler.remove(old.id);
            NotificationScheduler.add(item);
        }
    }

    public synchronized void update(T item) {
        int index = indexOf(item.id);
        if (index == -1) {
            add(item);
            return;
        }

        set(index, item);
    }

    public synchronized void remove(int index) {
        NotificationScheduler.remove(items.remove(index).id);
    }

    public synchronized boolean remove(I id) {
        int index = indexOf(id);
        if (index == -1) {
            return false;
        }

        remove(index);
        return true;
    }

    public synchronized void clear() {
        for (T item : items) {
            NotificationScheduler.remove(item.id);
        }

        items.clear();
    }

    public synchronized void setEntries(List<T> items) {
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

    public synchronized List<T> getEntries() {
        return Collections.unmodifiableList(items);
    }
}
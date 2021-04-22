package com.gmail.comcorecrew.comcore.classes.modules;

import androidx.preference.PreferenceFragmentCompat;

import com.gmail.comcorecrew.comcore.R;
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.EventItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.notifications.NotificationScheduler;
import com.gmail.comcorecrew.comcore.notifications.ScheduledList;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.EventID;

import java.util.ArrayList;
import java.util.List;

public class Calendar extends Module {

    private transient ScheduledList<EventID, EventEntry> approved;
    private transient ArrayList<EventEntry> unapproved;

    public Calendar(String name, CalendarID calendarID, Group group) {
        super(name, calendarID, group, Mdid.CCLD);
        approved = new ScheduledList<>();
        unapproved = new ArrayList<>();
    }

    public Calendar(String name, Group group) {
        super(name, group, Mdid.CCLD);
        approved = new ScheduledList<>();
        unapproved = new ArrayList<>();
    }

    public void approve(EventID eventID) {
        ServerConnector.approveEvent(eventID, true, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }

            onEventApproved(eventID);
        });
    }

    public void disapprove(EventID eventID) {
        ServerConnector.approveEvent(eventID, false, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }

            onEventDeleted(eventID);
        });
    }

    public void sendEvent(String description, long start, long end) {
        ServerConnector.addEvent((CalendarID) getId(), description, start, end, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }

            onEventAdded(result.data);
        });
    }

    public void deleteEvent(EventID eventID) {
        ServerConnector.deleteEvent(eventID, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }

            onEventDeleted(eventID);
        });
    }

    public List<EventEntry> getEntriesByDay(java.util.Calendar currentDay) {
        if (currentDay == null) {
            return getApproved();
        }

        ArrayList<EventEntry> eventList = new ArrayList<>();

        java.util.Calendar startDay = java.util.Calendar.getInstance();

        for (int i = 0; i < getApproved().size(); i++) {
            startDay.setTimeInMillis(approved.get(i).start);

            /* Currently gets entries based on their starting day
              TODO match entries as long as the currentDay overlaps with its time range */
            if (currentDay.get(java.util.Calendar.YEAR) == startDay.get(java.util.Calendar.YEAR) &&
                currentDay.get(java.util.Calendar.MONTH) == startDay.get(java.util.Calendar.MONTH) &&
                currentDay.get(java.util.Calendar.DATE) == startDay.get(java.util.Calendar.DATE)) {

                eventList.add(approved.get(i));
            }
        }
        return eventList;
    }

    public ArrayList<EventEntry> getInBulletin () {
        ArrayList<EventEntry> inBulletin = new ArrayList<>();
        for (int i = 0; i < approved.size(); i++) {
            if (approved.get(i).bulletin) {
                inBulletin.add(approved.get(i));
            }
        }
        return inBulletin;
    }

    public void addToBulletin(EventID id, boolean setBulletin) {
        ServerConnector.setBulletin(id, setBulletin, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(R.string.error_cannot_connect);
            }
        });
    }

    public List<EventEntry> getApproved() {
        return approved.getEntries();
    }

    public List<EventEntry> getUnapproved() {
        return unapproved;
    }

    private EventEntry removeUnapproved(EventID event) {
        for (int i = 0, s = unapproved.size(); i < s; i++) {
            EventEntry entry = unapproved.get(i);
            if (entry.id.equals(event)) {
                unapproved.remove(i);
                return entry;
            }
        }

        return null;
    }

    @Override
    public void didUpdate() {
        super.didUpdate();
        NotificationScheduler.store();
    }

    @Override
    public void onEventAdded(EventEntry event) {
        if (!event.id.module.equals(getId())) {
            return;
        }

        if (event.approved) {
            approved.add(event);
        } else {
            unapproved.add(event);
        }

        toCache();
    }

    @Override
    public void onEventApproved(EventID event) {
        if (!event.module.equals(getId())) {
            return;
        }

        EventEntry entry = removeUnapproved(event);
        if (entry != null) {
            approved.add(new EventEntry(
                    entry.id,
                    entry.creator,
                    entry.description,
                    entry.start,
                    entry.end,
                    true,
                    entry.bulletin));
            toCache();
        }
    }

    @Override
    public void onEventDeleted(EventID event) {
        if (!event.module.equals(getId())) {
            return;
        }

        if (!approved.remove(event)) {
            if (removeUnapproved(event) == null) {
                return;
            }
        }

        toCache();
    }

    @Override
    protected void readToCache() {
        if (approved.isEmpty() && unapproved.isEmpty()) {
            return;
        }

        ArrayList<Cacheable> items = new ArrayList<>();
        for (EventEntry event : approved.getEntries()) {
            items.add(new EventItem(event));
        }
        for (EventEntry event : unapproved) {
            items.add(new EventItem(event));
        }
        Cacher.cacheData(items, this);
    }

    @Override
    protected void readFromCache() {
        if (approved == null) {
            approved = new ScheduledList<>();
        }
        if (unapproved == null) {
            unapproved = new ArrayList<>();
        }

        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        unapproved.clear();
        ArrayList<EventEntry> entries = new ArrayList<>();
        CalendarID calendar = (CalendarID) getId();
        for (char[] line : data) {
            EventEntry entry = new EventItem(line).toEntry(calendar);
            if (entry.approved) {
                entries.add(entry);
            } else {
                unapproved.add(entry);
            }
        }
        approved.setEntries(entries);
    }

    @Override
    public void refresh() {
        ServerConnector.getEvents((CalendarID) getId(), result -> {
            if (result.isFailure()) {
                return;
            }

            unapproved.clear();
            ArrayList<EventEntry> approved = new ArrayList<>();
            for (EventEntry entry : result.data) {
                if (entry.approved) {
                    approved.add(entry);
                } else {
                    unapproved.add(entry);
                }
            }
            this.approved.setEntries(approved);
            toCache();
        });
    }

    @Override
    public void onDeleted() {
        approved.expensiveDeleteAll();
        unapproved.clear();
    }
}
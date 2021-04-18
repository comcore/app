package com.gmail.comcorecrew.comcore.classes.modules;

import android.app.usage.UsageEvents;
import android.icu.text.DateFormat;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.EventItem;
import com.gmail.comcorecrew.comcore.caching.TaskItem;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.enums.Reaction;
import com.gmail.comcorecrew.comcore.notifications.NotificationScheduler;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.EventID;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;

/*
 * UNDER CONSTRUCTION
 *
 * TRESPASSERS WILL BE PROSECUTED
 */
public class Calendar extends Module {

    private transient ArrayList<EventItem> events;

    public Calendar(String name, CalendarID calendarID, Group group) {
        super(name, calendarID, group, Mdid.CCLD);
        events = new ArrayList<>();
    }

    public Calendar(String name, Group group) {
        super(name, group, Mdid.CCLD);
        events = new ArrayList<>();
    }

    public ArrayList<EventItem> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<EventItem> events) {
        this.events = events;
    }

    public int getEventIndex(EventID eventID) {
        for (int i = 0; i < events.size(); i++) {
            if (eventID.id == events.get(i).getEventId()) {
                return i;
            }
        }
        return -1;
    }

    public EventEntry getEventEntry(EventID eventID) {
        int index = getEventIndex(eventID);
        if (index == -1) {
            return null;
        }
        return events.get(index).toEntry((CalendarID) getId());
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

            addEvent(result.data);
        });
    }

    public void addEvent(EventEntry event) {
        events.add(new EventItem(event));
        NotificationScheduler.add(event);
        toCache();
    }

    public void addEvents(ArrayList<EventEntry> events) {
        for (EventEntry event : events) {
            this.events.add(new EventItem(event));
            NotificationScheduler.add(event);
        }
        toCache();
    }

    public void deleteEvent(EventID eventID) {
        ServerConnector.deleteEvent(eventID, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }

            events.remove(getEventIndex(eventID));
            NotificationScheduler.remove(eventID);
            toCache();
        });
    }

    public void modifyEvent(EventID eventID, long start, long end, String description) {
        int index = getEventIndex(eventID);
        EventItem event = events.get(index);
        if (start < 0) {
            start = event.getStart();
        }
        if (end < 0) {
            end = event.getEnd();
        }
        if (description == null) {
            description = event.getData();
        }
        deleteEvent(eventID);
        sendEvent(description, start, end);
    }

    public ArrayList<EventEntry> getEntries() {
        ArrayList<EventEntry> entries = new ArrayList<>();
        for (EventItem event : events) {
            if (event.isApproved()) {
                entries.add(event.toEntry((CalendarID) getId()));
            }
        }
        return entries;
    }

    public ArrayList<EventEntry> getEntriesByDay(java.util.Calendar currentDay) {
        ArrayList<EventEntry> eventList = new ArrayList<>();

        java.util.Calendar startDay = java.util.Calendar.getInstance();

        for (int i = 0; i < getEntries().size(); i++) {
            startDay.setTimeInMillis(getEntries().get(i).start);

            /** Currently gets entries based on their starting day
             * TODO match entries as long as the currentDay overlaps with its time range **/
            if (currentDay.get(java.util.Calendar.YEAR) == startDay.get(java.util.Calendar.YEAR) &&
                currentDay.get(java.util.Calendar.MONTH) == startDay.get(java.util.Calendar.MONTH) &&
                currentDay.get(java.util.Calendar.DATE) == startDay.get(java.util.Calendar.DATE)) {

                eventList.add(getEntries().get(i));
            }
        }
        return eventList;
    }

    public ArrayList<EventEntry> getRequests() {
        ArrayList<EventEntry> entries = new ArrayList<>();
        for (EventItem event : events) {
            if (!event.isApproved()) {
                entries.add(event.toEntry((CalendarID) getId()));
            }
        }
        return entries;
    }

    @Override
    public void onEventAdded(EventEntry event) {
        if (!event.id.module.equals(getId())) {
            return;
        }

        addEvent(event);
    }

    @Override
    public void onEventApproved(EventID event) {
        if (!event.module.equals(getId())) {
            return;
        }

        int index = getEventIndex(event);
        if (index == -1) {
            return;
        }

        EventItem item = events.get(index);
        item.setApproved(true);
        NotificationScheduler.add(item.toEntry((CalendarID) getId()));
        toCache();
    }

    @Override
    public void onEventDeleted(EventID event) {
        if (!event.module.equals(getId())) {
            return;
        }

        int index = getEventIndex(event);
        if (index == -1) {
            return;
        }

        events.remove(index);
        NotificationScheduler.remove(event);
        toCache();
    }

    @Override
    protected void readToCache() {
        if (events.size() == 0) {
            return;
        }

        Cacher.cacheData(new ArrayList<>(events), this);
    }

    @Override
    protected void readFromCache() {
        events = new ArrayList<>();
        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        for (char[] line : data) {
            events.add(new EventItem(line));
        }
        registerNotifications();
    }

    @Override
    public void refresh() {
        ServerConnector.getEvents((CalendarID) getId(), result -> {
            if (result.isFailure()) {
                return;
            }

            events.clear();
            for (EventEntry eventEntry : result.data) {
                events.add(new EventItem(eventEntry));
            }
            registerNotifications();
            toCache();
        });
    }

    private void registerNotifications() {
        for (EventItem event : events) {
            NotificationScheduler.add(event.toEntry((CalendarID) getId()));
        }
    }
}
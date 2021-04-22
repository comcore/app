package com.gmail.comcorecrew.comcore.server.entry;

import androidx.core.app.NotificationCompat;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.notifications.NotificationHandler;
import com.gmail.comcorecrew.comcore.notifications.NotificationScheduler;
import com.gmail.comcorecrew.comcore.notifications.ScheduledNotification;
import com.gmail.comcorecrew.comcore.server.id.EventID;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of event data returned by the server.
 */
public final class EventEntry extends ModuleEntry<CalendarID, EventID> {
    /**
     * The user that created the event.
     */
    public final UserID creator;

    /**
     * The description of the event.
     */
    public final String description;

    /**
     * The UNIX timestamp representing when event starts.
     */
    public final long start;

    /**
     * The UNIX timestamp representing when event ends.
     */
    public final long end;

    /**
     * Whether the event has been approved by a moderator.
     */
    public final boolean approved;

    /**
     * Whether the event should appear on a bulletin board regardless of the start time.
     */
    public final boolean bulletin;

    /**
     * Create an EventEntry with a description of the event and whether it has been approved.
     *
     * @param id          the EventID of the event
     * @param creator     the user that created the event
     * @param description the description of the event
     * @param start       the start timestamp
     * @param end         the end timestamp
     * @param approved    whether the event has been approved by a moderator
     * @param bulletin    whether the event should appear on a bulletin board
     */
    public EventEntry(EventID id, UserID creator, String description, long start, long end,
                      boolean approved, boolean bulletin) {
        super(id);

        if (creator == null) {
            throw new IllegalArgumentException("event creator cannot be null");
        } else if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("event description cannot be null or empty");
        } else if (start < 1) {
            throw new IllegalArgumentException("event start timestamp cannot be less than 1");
        } else if (end < start) {
            throw new IllegalArgumentException("event end cannot come before start");
        }

        this.creator = creator;
        this.description = description;
        this.start = start;
        this.end = end;
        this.approved = approved;
        this.bulletin = bulletin;
    }

    /**
     * Create a EventEntry from a JsonObject. If specified, the given CalendarID is used. Otherwise,
     * it is taken from the message data.
     *
     * @param calendar the calendar which contains the event or null
     * @param json     the data sent by the server
     * @return the EventEntry
     */
    public static EventEntry fromJson(CalendarID calendar, JsonObject json) {
        EventID id = EventID.fromJson(calendar, json);
        UserID creator = new UserID(json.get("owner").getAsString());
        String description = json.get("description").getAsString();
        long start = json.get("start").getAsLong();
        long end = json.get("end").getAsLong();
        boolean approved = json.get("approved").getAsBoolean();
        boolean bulletin = json.get("bulletin").getAsBoolean();
        return new EventEntry(id, creator, description, start, end, approved, bulletin);
    }

    @Override
    public ScheduledNotification getScheduledNotification() {
        if (!approved) {
            return null;
        }

        long displayTime = start - NotificationScheduler.REMINDER_TIME;
        if (displayTime < System.currentTimeMillis()) {
            return null;
        }

        Module module = GroupStorage.getModule(id.module);
        if (module == null || module.isMuted()) {
            return null;
        }

        return new ScheduledNotification(
                NotificationHandler.CHANNEL_EVENT,
                NotificationCompat.PRIORITY_HIGH,
                displayTime,
                module.getName(),
                "Upcoming event: " + description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventEntry that = (EventEntry) o;
        return start == that.start &&
                end == that.end &&
                approved == that.approved &&
                bulletin == that.bulletin &&
                creator.equals(that.creator) &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creator, description, start, end, approved, bulletin);
    }
}
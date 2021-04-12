package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.EventID;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of event data returned by the server.
 */
public final class EventEntry {
    /**
     * The event's identifier.
     */
    public final EventID id;

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
     * Create an EventEntry with a description of the event and whether it has been approved.
     *
     * @param id          the EventID of the event
     * @param creator     the user that created the event
     * @param description the description of the event
     * @param start       the start timestamp
     * @param end         the end timestamp
     * @param approved    whether the event has been approved by a moderator
     */
    public EventEntry(EventID id, UserID creator, String description, long start, long end,
                      boolean approved) {
        if (id == null) {
            throw new IllegalArgumentException("EventID cannot be null");
        } else if (creator == null) {
            throw new IllegalArgumentException("event creator cannot be null");
        } else if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("event description cannot be null or empty");
        } else if (start < 1) {
            throw new IllegalArgumentException("event start timestamp cannot be less than 1");
        } else if (end < start) {
            throw new IllegalArgumentException("event end cannot come before start");
        }

        this.id = id;
        this.creator = creator;
        this.description = description;
        this.start = start;
        this.end = end;
        this.approved = approved;
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
        UserID creator = new UserID(json.get("creator").getAsString());
        String description = json.get("description").getAsString();
        long start = json.get("start").getAsLong();
        long end = json.get("end").getAsLong();
        boolean approved = json.get("approved").getAsBoolean();
        return new EventEntry(id, creator, description, start, end, approved);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventEntry that = (EventEntry) o;
        return start == that.start &&
                end == that.end &&
                approved == that.approved &&
                id.equals(that.id) &&
                creator.equals(that.creator) &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creator, description, start, end, approved);
    }
}
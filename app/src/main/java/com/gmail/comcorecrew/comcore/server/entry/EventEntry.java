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
     * The UNIX timestamp representing when event starts.
     */
    public final long timestamp;

    /**
     * The description of the event.
     */
    public final String description;

    /**
     * Whether the event has been approved by a moderator.
     */
    public final boolean approved;

    /**
     * Create an EventEntry with a description of the event and whether it has been approved.
     *
     * @param id          the EventID of the event
     * @param description the description of the task
     * @param approved    whether the event has been approved by a moderator
     */
    public EventEntry(EventID id, long timestamp, String description, boolean approved) {
        if (id == null) {
            throw new IllegalArgumentException("EventID cannot be null");
        } else if (timestamp < 1) {
            throw new IllegalArgumentException("event timestamp cannot be less than 1");
        } else if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("event description cannot be null or empty");
        }

        this.id = id;
        this.timestamp = timestamp;
        this.description = description;
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
        long timestamp = json.get("timestamp").getAsLong();
        String description = json.get("description").getAsString();
        boolean approved = json.get("approved").getAsBoolean();
        return new EventEntry(id, timestamp, description, approved);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventEntry that = (EventEntry) o;
        return timestamp == that.timestamp &&
                approved == that.approved &&
                id.equals(that.id) &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, description, approved);
    }
}
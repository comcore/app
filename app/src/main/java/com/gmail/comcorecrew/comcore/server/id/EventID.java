package com.gmail.comcorecrew.comcore.server.id;

import com.google.gson.JsonObject;

/**
 * Represents an identifier for an event in a calendar.
 */
public class EventID extends ModuleItemID<CalendarID> {
    /**
     * Create an EventID from a parent calendar and a numeric ID.
     *
     * @param calendar the parent calendar
     * @param id       the numeric ID
     */
    public EventID(CalendarID calendar, long id) {
        super(calendar, id);
    }

    /**
     * Parse a EventID from a JsonObject. If specified, the given CalendarID is used. Otherwise,
     * it is taken from the message data.
     *
     * @param calendar the calendar which contains the event or null
     * @param json     the data sent by the server
     * @return the EventID
     */
    public static EventID fromJson(CalendarID calendar, JsonObject json) {
        if (calendar == null) {
            GroupID group = new GroupID(json.get("group").getAsString());
            calendar = new CalendarID(group, json.get("calendar").getAsString());
        }

        long id = json.get("id").getAsLong();
        return new EventID(calendar, id);
    }
}
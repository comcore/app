package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.CalendarID;
import com.gmail.comcorecrew.comcore.server.id.EventID;

public class EventItem implements Cacheable {

    private int userId;
    private long eventId;
    private long start;
    private long end;
    private boolean approved;
    private String data;

    public EventItem(EventEntry entry) {
        userId = UserStorage.getInternalId(entry.creator);
        eventId = entry.id.id;
        start = entry.start;
        end = entry.end;
        approved = entry.approved;
        data = entry.description;
    }

    public EventItem(char[] cache) {
        if (cache.length < 15) {
            throw new IllegalArgumentException("Cache too short");
        }

        int index = 0;
        userId = cache[index++];
        userId = (userId << 16) | cache[index++];
        eventId = cache[index++];
        eventId = (eventId << 16) | cache[index++];
        eventId = (eventId << 16) | cache[index++];
        eventId = (eventId << 16) | cache[index++];
        start = cache[index++];
        start = (start << 16) | cache[index++];
        start = (start << 16) | cache[index++];
        start = (start << 16) | cache[index++];
        end = cache[index++];
        end = (end << 16) | cache[index++];
        end = (end << 16) | cache[index++];
        end = (end << 16) | cache[index++];
        approved = cache[index++] != 0;
        data = new String(cache, index, cache.length - index);

    }

    public char[] toCache() {
        char[] cache = new char[2 + 4 + 4 + 4 + 1 + data.length()];
        int index = 0;

        cache[index++] = (char) (userId >> 16);
        cache[index++] = (char) userId;
        cache[index++] = (char) (eventId >> 48);
        cache[index++] = (char) (eventId >> 32);
        cache[index++] = (char) (eventId >> 16);
        cache[index++] = (char) eventId;
        cache[index++] = (char) (start >> 48);
        cache[index++] = (char) (start >> 32);
        cache[index++] = (char) (start >> 16);
        cache[index++] = (char) start;
        cache[index++] = (char) (end >> 48);
        cache[index++] = (char) (end >> 32);
        cache[index++] = (char) (end >> 16);
        cache[index++] = (char) end;
        if (approved) {
            cache[index++] = 1;
        }
        else {
            cache[index++] = 0;
        }
        for (int i = 0; i < data.length(); i++) {
            cache[index++] = data.charAt(i);
        }

        return cache;
    }

    public EventEntry toEntry(CalendarID calendarID) {
        EventID eventID = new EventID(calendarID, eventId);
        return new EventEntry(eventID, UserStorage.getUser(userId).getID(), data, start, end, approved);
    }

    public int getUserId() {
        return userId;
    }

    public long getEventId() {
        return eventId;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getData() {
        return data;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setData(String data) {
        this.data = data;
    }
}

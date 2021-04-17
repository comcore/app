package com.gmail.comcorecrew.comcore.notifications;

import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents a notification that can be scheduled to occur at a specific time.
 */
public final class ScheduledNotification {
    /**
     * The notification channel ID (from NotificationHandler). Not considered during comparison.
     */
    public final String channel;

    /**
     * The priority level (from NotificationCompat). Not considered during comparison.
     */
    public final int priority;

    /**
     * The timestamp for when the notification should display.
     */
    public final long timestamp;

    /**
     * The title of the notification.
     */
    public final String title;

    /**
     * The contents of the notification.
     */
    public final String text;

    /**
     * Create a new ScheduledNotification.
     *
     * @param channel   the notification channel
     * @param priority  the priority level
     * @param timestamp the timestamp
     * @param title     the title
     * @param text      the text
     */
    public ScheduledNotification(String channel, int priority, long timestamp, String title,
                                 String text) {
        if (channel == null || channel.isEmpty()) {
            throw new IllegalArgumentException("notification channel cannot be null or empty");
        } else if (timestamp < 1) {
            throw new IllegalArgumentException("notification timestamp cannot be less than 1");
        } else if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("notification title cannot be null or empty");
        } else if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("notification text cannot be null or empty");
        }

        this.channel = channel;
        this.priority = priority;
        this.timestamp = timestamp;
        this.title = title;
        this.text = text;
    }

    /**
     * Create a ScheduledNotification from a JsonObject.
     *
     * @param json the stored JSON
     * @return the ScheduledNotification
     */
    public static ScheduledNotification fromJson(JsonObject json) {
        String channel = json.get("channel").getAsString();
        int priority = json.get("priority").getAsInt();
        long timestamp = json.get("timestamp").getAsLong();
        String title = json.get("title").getAsString();
        String text = json.get("text").getAsString();
        return new ScheduledNotification(channel, priority, timestamp, title, text);
    }

    /**
     * Convert the ScheduledNotification to a JsonObject.
     *
     * @return the JSON object to be stored
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("channel", channel);
        json.addProperty("priority", priority);
        json.addProperty("timestamp", timestamp);
        json.addProperty("title", title);
        json.addProperty("text", text);
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledNotification that = (ScheduledNotification) o;
        return timestamp == that.timestamp &&
                title.equals(that.title) &&
                text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, title, text);
    }
}
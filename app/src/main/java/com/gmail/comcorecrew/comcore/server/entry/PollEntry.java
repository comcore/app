package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.notifications.ScheduledNotification;
import com.gmail.comcorecrew.comcore.server.id.PollID;
import com.gmail.comcorecrew.comcore.server.id.PollListID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an entry of poll data returned by the server.
 */
public final class PollEntry extends ModuleEntry<PollListID, PollID> {
    /**
     * The user that created the poll.
     */
    public final UserID creator;

    /**
     * The UNIX timestamp representing when the poll was last modified.
     */
    public final long timestamp;

    /**
     * The description of the poll.
     */
    public final String description;

    /**
     * The options of the poll.
     */
    public final List<PollOption> options;

    /**
     * The index of the option the user voted for, or -1 if they haven't voted yet.
     */
    public final int vote;

    /**
     * Create a PollEntry with an owner, a description of the poll, and the associated options.
     *
     * @param id          the PollID of the poll
     * @param creator     the user that created the poll
     * @param timestamp   the timestamp that the poll was last modified
     * @param description the description of the poll
     * @param options     the options available in the poll
     * @param vote        the user's vote (or -1)
     */
    public PollEntry(PollID id, UserID creator, long timestamp, String description,
                     List<PollOption> options, int vote) {
        super(id);

        if (creator == null) {
            throw new IllegalArgumentException("poll creator cannot be null");
        } else if (timestamp < 1) {
            throw new IllegalArgumentException("poll timestamp cannot be less than 1");
        } else if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("poll description cannot be null or empty");
        } else if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("poll options cannot be null or empty");
        } else if (vote < -1 || vote >= options.size()) {
            throw new IllegalArgumentException("poll vote out of range for options");
        }

        this.creator = creator;
        this.timestamp = timestamp;
        this.description = description;
        this.options = options;
        this.vote = vote;
    }

    /**
     * Create a PollEntry from a JsonObject. If specified, the given PollListID is used. Otherwise,
     * it is taken from the message data.
     *
     * @param pollList the poll list which contains the poll or null
     * @param json     the data sent by the server
     * @return the PollEntry
     */
    public static PollEntry fromJson(PollListID pollList, JsonObject json) {
        PollID id = PollID.fromJson(pollList, json);
        UserID creator = new UserID(json.get("owner").getAsString());
        long timestamp = json.get("timestamp").getAsLong();
        String description = json.get("description").getAsString();
        ArrayList<PollOption> options = new ArrayList<>();
        for (JsonElement option : json.getAsJsonArray("options")) {
            options.add(PollOption.fromJson(option.getAsJsonObject()));
        }
        JsonElement voteJson = json.get("vote");
        int vote = voteJson.isJsonNull() ? -1 : voteJson.getAsInt();
        return new PollEntry(id, creator, timestamp, description, options, vote);
    }

    @Override
    public ScheduledNotification getScheduledNotification() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PollEntry pollEntry = (PollEntry) o;
        return timestamp == pollEntry.timestamp &&
                id.equals(pollEntry.id) &&
                creator.equals(pollEntry.creator) &&
                description.equals(pollEntry.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creator, timestamp, description);
    }
}
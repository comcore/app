package com.gmail.comcorecrew.comcore.server.entry;

import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an option in a PollEntry.
 */
public class PollOption {
    /**
     * The name of the poll option.
     */
    public final String description;

    /**
     * The number of people who have voted for this option.
     */
    public final int numberOfVotes;

    /**
     * Create a poll option from a description and a number of votes.
     *
     * @param description   the description of the option
     * @param numberOfVotes the number of votes for the option
     */
    public PollOption(String description, int numberOfVotes) {
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("poll description cannot be empty");
        } else if (numberOfVotes < 0) {
            throw new IllegalArgumentException("poll number of votes cannot be negative");
        }
        this.description = description;
        this.numberOfVotes = numberOfVotes;
    }

    /**
     * Create a PollOption from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the PollOption
     */
    public static PollOption fromJson(JsonObject json) {
        String description = json.get("description").getAsString();
        int numberOfVotes = json.get("numberOfVotes").getAsInt();
        return new PollOption(description, numberOfVotes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PollOption that = (PollOption) o;
        return numberOfVotes == that.numberOfVotes &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, numberOfVotes);
    }
}
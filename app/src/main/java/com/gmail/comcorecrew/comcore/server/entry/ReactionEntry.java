package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.enums.Reaction;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of reaction data returned by the server.
 */
public final class ReactionEntry {
    /**
     * The user that sent this reaction.
     */
    public final UserID user;

    /**
     * The reaction of the user.
     */
    public final String reaction;

    /**
     * Create a ReactionEntry from just a reaction. The user will be set to null.
     *
     * @param reaction the user's reaction
     */
    public ReactionEntry(String reaction) {
        if (reaction == null || reaction.isEmpty()) {
            throw new IllegalArgumentException("reaction cannot be null or empty");
        }

        this.user = null;
        this.reaction = reaction;
    }

    /**
     * Create a ReactionEntry from a user and their reaction.
     *
     * @param user     the user that sent the reaction
     * @param reaction the user's reaction
     */
    public ReactionEntry(UserID user, String reaction) {
        if (user == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        } else if (reaction == null || reaction.isEmpty()) {
            throw new IllegalArgumentException("reaction cannot be null or empty");
        }

        this.user = user;
        this.reaction = reaction;
    }

    /**
     * Get a Reaction value from this user's reaction. An unknown reaction kind will be returned
     * as UNKNOWN. The reaction NONE will never be returned.
     *
     * @return the parsed Reaction
     */
    public Reaction getReaction() {
        for (Reaction reaction : Reaction.reactions) {
            if (this.reaction.equalsIgnoreCase(reaction.name())) {
                return reaction;
            }
        }

        return Reaction.UNKNOWN;
    }

    /**
     * Create a ReactionEntry from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the ReactionEntry
     */
    public static ReactionEntry fromJson(JsonObject json) {
        UserID user = new UserID(json.get("user").getAsString());
        String reaction = json.get("reaction").getAsString();
        return new ReactionEntry(user, reaction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReactionEntry that = (ReactionEntry) o;
        return Objects.equals(user, that.user) &&
                reaction.equals(that.reaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, reaction);
    }
}
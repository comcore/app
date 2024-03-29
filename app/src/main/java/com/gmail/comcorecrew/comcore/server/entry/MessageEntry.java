package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.notifications.ScheduledNotification;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an entry of message data returned by the server.
 */
public final class MessageEntry extends ModuleEntry<ChatID, MessageID> {
    /**
     * The user that sent the message.
     */
    public final UserID sender;

    /**
     * The UNIX timestamp representing when the message was sent or edited.
     */
    public final long timestamp;

    /**
     * The contents of the message.
     */
    public final String contents;

    /**
     * The reactions of users to the message. This set is immutable, so any attempts to modify it
     * will result in an exception.
     */
    public final Map<UserID, String> reactions;

    /**
     * Create a MessageEntry with information about who sent it and when. An empty contents
     * represents a deleted message.
     *
     * @param id        the MessageID of the message
     * @param sender    the user that sent the message
     * @param timestamp the timestamp from when the message was sent
     * @param contents  the contents of the message (or empty)
     * @param reactions the reactions to the message
     */
    public MessageEntry(MessageID id, UserID sender, long timestamp, String contents,
                        Map<UserID, String> reactions) {
        super(id);

        if (sender == null) {
            throw new IllegalArgumentException("message sender cannot be null");
        } else if (timestamp < 1) {
            throw new IllegalArgumentException("message timestamp cannot be less than 1");
        } else if (contents == null) {
            throw new IllegalArgumentException("message contents cannot be null");
        } else if (reactions == null) {
            throw new IllegalArgumentException("reactions cannot be null");
        }

        this.sender = sender;
        this.timestamp = timestamp;
        this.contents = contents;
        this.reactions = Collections.unmodifiableMap(reactions);
    }

    /**
     * Create a MessageEntry from a JsonObject. If specified, the given ChatID is used. Otherwise,
     * it is taken from the message data.
     *
     * @param chat the chat which the message was retrieved from or null
     * @param json the data sent by the server
     * @return the MessageEntry
     */
    public static MessageEntry fromJson(ChatID chat, JsonObject json) {
        MessageID id = MessageID.fromJson(chat, json);
        UserID sender = new UserID(json.get("sender").getAsString());
        long timestamp = json.get("timestamp").getAsLong();
        String contents = json.get("contents").getAsString();
        Map<UserID, String> reactions = parseReactions(json);
        return new MessageEntry(id, sender, timestamp, contents, reactions);
    }

    /**
     * Parse a map of reactions from a JsonObject.
     *
     * @param json the data send by the server
     * @return the Map of UserIDs to reaction Strings
     */
    public static Map<UserID, String> parseReactions(JsonObject json) {
        HashMap<UserID, String> reactions = new HashMap<>();
        for (JsonElement reaction : json.getAsJsonArray("reactions")) {
            JsonObject reactionJson = reaction.getAsJsonObject();
            reactions.put(
                    new UserID(reactionJson.get("user").getAsString()),
                    reactionJson.get("reaction").getAsString());
        }

        return reactions;
    }

    @Override
    public ScheduledNotification getScheduledNotification() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageEntry that = (MessageEntry) o;
        return timestamp == that.timestamp &&
                id.equals(that.id) &&
                sender.equals(that.sender) &&
                contents.equals(that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sender, timestamp, contents);
    }
}
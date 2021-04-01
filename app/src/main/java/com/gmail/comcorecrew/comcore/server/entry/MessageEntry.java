package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of message data returned by the server.
 */
public final class MessageEntry {
    /**
     * The message's identifier.
     */
    public final MessageID id;

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
     * Create a MessageEntry with information about who sent it and when.
     *
     * @param id        the MessageID of the message
     * @param sender    the user that sent the message
     * @param timestamp the timestamp from when the message was sent
     * @param contents  the contents of the message
     */
    public MessageEntry(MessageID id, UserID sender, long timestamp, String contents) {
        if (id == null) {
            throw new IllegalArgumentException("MessageID cannot be null");
        } else if (sender == null) {
            throw new IllegalArgumentException("message sender cannot be null");
        } else if (timestamp < 1) {
            throw new IllegalArgumentException("message timestamp cannot be less than 1");
        } else if (contents == null || contents.isEmpty()) {
            throw new IllegalArgumentException("message contents cannot be null or empty");
        }

        this.id = id;
        this.sender = sender;
        this.timestamp = timestamp;
        this.contents = contents;
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
        return new MessageEntry(id, sender, timestamp, contents);
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
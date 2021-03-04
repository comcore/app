package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of message data returned by the server.
 */
public final class MessageEntry {
    /**
     * The chat that the message was sent in.
     */
    public final ChatID chat;

    /**
     * The user that sent the message.
     */
    public final UserEntry sender;

    /**
     * The UNIX timestamp representing when the message was sent.
     */
    public final long timestamp;

    /**
     * The contents of the message.
     */
    public final String contents;

    /**
     * Create a MessageEntry with information about who sent it and when.
     *
     * @param chat      the chat that the message was sent in
     * @param sender    the user that sent the message
     * @param timestamp the timestamp from when the message was sent
     * @param contents  the contents of the message
     */
    public MessageEntry(ChatID chat, UserEntry sender, long timestamp, String contents) {
        if (chat == null) {
            throw new IllegalArgumentException("ChatID cannot be null");
        } else if (sender == null) {
            throw new IllegalArgumentException("message sender cannot be null");
        } else if (timestamp == 0) {
            throw new IllegalArgumentException("message timestamp cannot be 0");
        } else if (contents == null || contents.isEmpty()) {
            throw new IllegalArgumentException("message contents cannot be null or empty");
        }

        this.sender = sender;
        this.chat = chat;
        this.timestamp = timestamp;
        this.contents = contents;
    }

    /**
     * Create a MessageEntry from a JSON object.
     *
     * @param json the data sent by the server
     */
    public static MessageEntry fromJson(JsonObject json) {
        GroupID group = new GroupID(json.get("group").getAsString());
        ChatID chat = new ChatID(group, json.get("chat").getAsString());
        UserEntry sender = UserEntry.fromJson(json.get("sender").getAsJsonObject());
        long timestamp = json.get("timestamp").getAsLong();
        String contents = json.get("contents").getAsString();
        return new MessageEntry(chat, sender, timestamp, contents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageEntry that = (MessageEntry) o;
        return timestamp == that.timestamp &&
                chat.equals(that.chat) &&
                sender.equals(that.sender) &&
                contents.equals(that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat, sender, timestamp, contents);
    }
}
package com.gmail.comcorecrew.comcore.server.id;

import androidx.annotation.NonNull;

/**
 * Represents a sequential identifier for a message in a chat.
 */
public class MessageID {
    /**
     * The maximum value of a MessageID (2^53). This is the limit because it is the largest integer
     * such that all smaller integers can be stored in a double-precision floating point number.
     */
    public static final long MAX_ID = 0x20_0000_0000_0000L;

    /**
     * The chat that this message was sent in.
     */
    public final ChatID chat;

    /**
     * The numeric ID corresponding to the message.
     */
    public final long id;

    /**
     * Create a MessageID from a parent chat and a numeric ID.
     * @param chat the parent chat
     * @param id   the numeric ID
     */
    public MessageID(ChatID chat, long id) {
        if (chat == null) {
            throw new IllegalArgumentException("ChatID cannot be null");
        } else if (id < 1 || id >= MAX_ID) {
            throw new IllegalArgumentException("message ID must be between 1 and 2^53");
        }

        this.chat = chat;
        this.id = id;
    }

    @Override
    @NonNull
    public String toString() {
        return Long.toString(id);
    }
}

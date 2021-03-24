package com.gmail.comcorecrew.comcore.server.id;

import com.google.gson.JsonObject;

/**
 * Represents a sequential identifier for a message in a chat.
 */
public final class MessageID extends ModuleItemID<ChatID> {
    /**
     * Create a MessageID from a parent chat and a numeric ID.
     *
     * @param chat the parent chat
     * @param id   the numeric ID
     */
    public MessageID(ChatID chat, long id) {
        super(chat, id);
    }

    /**
     * Helper function to check if this message comes immediately after a previous message with no
     * gap in between them.
     *
     * @param previousMessage a previous message
     * @return true if this message immediately follows the previous one, false otherwise
     */
    public boolean immediatelyAfter(MessageID previousMessage) {
        return previousMessage == null || id == previousMessage.id + 1;
    }

    /**
     * Parse a MessageID from a JsonObject. If specified, the given ChatID is used. Otherwise,
     * it is taken from the message data.
     *
     * @param chat the chat which the message was retrieved from or null
     * @param json the data sent by the server
     * @return the MessageID
     */
    public static MessageID fromJson(ChatID chat, JsonObject json) {
        if (chat == null) {
            GroupID group = new GroupID(json.get("group").getAsString());
            chat = new ChatID(group, json.get("chat").getAsString());
        }

        long id = json.get("id").getAsLong();
        return new MessageID(chat, id);
    }
}
package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.ChatID;

/**
 * Represents an entry of chat data returned by the server.
 */
public final class ChatEntry {
    /**
     * The chat's identifier.
     */
    public final ChatID id;

    /**
     * The chat's name.
     */
    public final String name;

    /**
     * Create a ChatEntry from a ChatID and a name
     * @param id   the ChatID of the chat
     * @param name the name of the chat
     */
    public ChatEntry(ChatID id, String name) {
        this.id = id;
        this.name = name;
    }
}
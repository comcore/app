package com.gmail.comcorecrew.comcore.server.entry;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.server.id.ChatID;

import java.util.Objects;

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
     * Create a ChatEntry from a ChatID and a name.
     *
     * @param id   the ChatID of the chat
     * @param name the name of the chat
     */
    public ChatEntry(ChatID id, String name) {
        if (id == null) {
            throw new IllegalArgumentException("ChatID cannot be null");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("chat name cannot be null or empty");
        }

        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatEntry chatEntry = (ChatEntry) o;
        return id.equals(chatEntry.id) &&
                name.equals(chatEntry.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
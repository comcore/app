package com.gmail.comcorecrew.comcore.notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single mention in a chat message.
 */
public class ChatMention {
    public static final String MENTION_ALL = "all";

    /**
     * The name that was mentioned.
     */
    public final String mentionName;

    /**
     * Create a ChatMention from a mentioned name.
     *
     * @param mentionName the name that was mentioned or "all"
     */
    public ChatMention(String mentionName) {
        this.mentionName = mentionName;
    }

    /**
     * Check if a user is the one being mentioned (or "all").
     *
     * @param name the name of the user
     * @return true if they are mentioned, false otherwise
     */
    public boolean mentionsUser(String name) {
        return mentionName.equals(name) || mentionName.equals(MENTION_ALL);
    }

    /**
     * Find all of the mentions in a message of the format @ followed by a word.
     *
     * @param message the message to parse
     * @return a list of all chat mentions
     */
    public static List<ChatMention> parseChatMentions(String message) {
        return new ArrayList<>();
    }
}
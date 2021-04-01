package com.gmail.comcorecrew.comcore.notifications;

import androidx.annotation.NonNull;

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
     * The index of the mention in the string.
     */
    public final int index;

    /**
     * Create a ChatMention from a mentioned name.
     *
     * @param mentionName the name that was mentioned or "all"
     */
    public ChatMention(String mentionName, int index) {
        this.mentionName = mentionName;
        this.index = index;
    }

    /**
     * Check if a user is the one being mentioned (or "all").
     *
     * @param name the name of the user
     * @return true if they are mentioned, false otherwise
     */
    public boolean mentionsUser(String name) {
        return mentionName.equals(MENTION_ALL) || mentionName.equals(getName(name, 0));
    }

    /**
     * Get the number of characters in the mention.
     *
     * @return the number of characters in the mention
     */
    public int length() {
        return mentionName.length() + 1;
    }

    /**
     * Extracts a first name from a string. Used for parsing mentions.
     *
     * @param message    the message
     * @param startIndex the start index to parse from
     * @return
     */
    private static String getName(String message, int startIndex) {
        int endIndex;
        for (endIndex = startIndex; endIndex < message.length(); endIndex++) {
            if (Character.isWhitespace(message.charAt(endIndex))) {
                break;
            }
        }

        if (endIndex == startIndex) {
            return null;
        } else {
            return message.substring(startIndex, endIndex);
        }
    }

    /**
     * Find all of the mentions in a message of the format @ followed by a word.
     *
     * @param message the message to parse
     * @return a list of all chat mentions
     */
    public static List<ChatMention> parseChatMentions(String message) {
        ArrayList<ChatMention> mentions = new ArrayList<>(0);
        int index = 0;
        while ((index = message.indexOf('@', index)) != -1) {
            int mentionIndex = index++;
            String mentionName = getName(message, index);
            if (mentionName != null) {
                index += mentionName.length();
                mentions.add(new ChatMention(mentionName, mentionIndex));
            }
        }
        return mentions;
    }

    @Override
    @NonNull
    public String toString() {
        return "@" + mentionName;
    }
}
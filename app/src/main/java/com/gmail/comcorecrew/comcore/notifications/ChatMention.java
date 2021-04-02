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
        return mentionName.equalsIgnoreCase(MENTION_ALL)
                || mentionName.equalsIgnoreCase(getName(name, 0));
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
     * @return the parsed name substring
     */
    private static String getName(String message, int startIndex) {
        int ch, offset;
        for (offset = 0; offset < message.length(); offset += Character.charCount(ch)) {
            ch = message.codePointAt(offset);

            if (!Character.isLetterOrDigit(ch)) {
                break;
            }
        }

        if (offset == startIndex) {
            return null;
        } else {
            return message.substring(startIndex, offset);
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
        System.out.println(mentions);
        return mentions;
    }

    @Override
    @NonNull
    public String toString() {
        return "@" + mentionName;
    }
}
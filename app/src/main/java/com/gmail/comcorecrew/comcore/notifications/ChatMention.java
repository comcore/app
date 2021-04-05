package com.gmail.comcorecrew.comcore.notifications;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single mention in a chat message.
 */
public class ChatMention {
    public static final String MENTION_ALL = "all";
    public static final char START_BLOCK = '<';
    public static final char END_BLOCK = '>';
    public static final int MENTION_COLOR = Color.GREEN;

    /**
     * Represents the two kinds of styles a mention can be.
     */
    public enum Style {
        WORD,
        BLOCK,
    }

    /**
     * The name that was mentioned.
     */
    public final String mentionName;

    /**
     * The style of the mention.
     */
    public final Style style;

    /**
     * The index of the mention in the string.
     */
    public final int index;

    /**
     * Create a ChatMention from a mentioned name.
     *
     * @param mentionName the name that was mentioned
     * @param style       the style of the mention
     * @param index       where the mention occurred in the string
     */
    public ChatMention(String mentionName, Style style, int index) {
        this.mentionName = mentionName;
        this.style = style;
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
                || mentionName.equalsIgnoreCase(name);
    }

    /**
     * Check if the mentioned user is contained in a group.
     *
     * @param group the group to check for the user in
     * @return true if they are in the group, false otherwise
     */
    public boolean containsMentioned(Group group) {
        if (mentionName.equalsIgnoreCase(MENTION_ALL)) {
            return true;
        }

        for (User user : group.getUsers()) {
            if (user.getName().equalsIgnoreCase(mentionName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the number of characters in the mention.
     *
     * @return the number of characters in the mention
     */
    public int length() {
        switch (style) {
            case WORD:
                return mentionName.length() + 1;
            case BLOCK:
                return mentionName.length() + 3;
            default:
                throw new IllegalStateException("invalid style");
        }
    }

    /**
     * Parse the contents of a mention from a string.
     *
     * @param message the message
     * @param atIndex the index of the at symbol
     * @return the parsed ChatMention
     */
    private static ChatMention parseMention(String message, int atIndex) {
        // Start parsing after the at symbol
        int startIndex = atIndex + 1;
        if (startIndex >= message.length()) {
            return null;
        }

        // Check if this is a BLOCK style mention
        if (message.charAt(startIndex) == START_BLOCK) {
            // Parse the contents of the block mention
            int offset;
            for (offset = startIndex + 1; offset < message.length(); offset++) {
                char ch = message.charAt(offset);
                if (ch == END_BLOCK) {
                    // If the block ends, then it is valid so return the parsed mention
                    String name = message.substring(startIndex + 1, offset);
                    return new ChatMention(name, Style.BLOCK, atIndex);
                } else if (ch == '@' || ch == START_BLOCK) {
                    // These characters are invalid in a block, so stop checking
                    break;
                }
            }

            // There was an invalid character or no closing delimiter, so it is invalid
            return null;
        }

        // Parse code point by code point until a non-alphanumeric unicode character is found
        int ch, offset;
        for (offset = startIndex; offset < message.length(); offset += Character.charCount(ch)) {
            ch = message.codePointAt(offset);
            if (!Character.isLetterOrDigit(ch)) {
                break;
            }
        }

        // If there was no name present, it is not valid
        if (offset == startIndex) {
            return null;
        }

        // Return a mention with the specified substring for the name
        String name = message.substring(startIndex, offset);
        return new ChatMention(name, Style.WORD, atIndex);
    }

    /**
     * Find all of the mentions in a message.
     *
     * @param message the message to parse
     * @return a list of all chat mentions
     */
    public static List<ChatMention> parseMentions(String message) {
        ArrayList<ChatMention> mentions = new ArrayList<>(0);

        // Iterate through all of the at symbols in the message to find all of the mentions
        int index = 0;
        while ((index = message.indexOf('@', index)) != -1) {
            ChatMention mention = parseMention(message, index);
            if (mention == null) {
                index++;
                continue;
            }

            mentions.add(mention);
            index += mention.length();
        }

        return mentions;
    }

    /**
     * Format a message to include a special color for mentions of users in the group.
     *
     * @param message the message to parse
     * @param group   the current group
     * @return the formatted string
     */
    public static CharSequence formatMentions(String message, Group group) {
        if (message == null || message.isEmpty()) {
            return "[deleted]";
        }

        // Update the text and add a span for each mention in reverse order
        SpannableStringBuilder builder = new SpannableStringBuilder(message);
        List<ChatMention> mentions = parseMentions(message);
        Collections.reverse(mentions);
        for (ChatMention mention : mentions) {
            // Only add the mention if they are part of the group
            if (mention.containsMentioned(group)) {
                int start = mention.index;
                int end = start + mention.length();
                ForegroundColorSpan color = new ForegroundColorSpan(MENTION_COLOR);
                builder.setSpan(color, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                builder.replace(start, end, mention.toString());
            }
        }

        return builder;
    }

    @Override
    @NonNull
    public String toString() {
        return "@" + mentionName;
    }
}
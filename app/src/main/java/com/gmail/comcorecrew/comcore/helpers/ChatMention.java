package com.gmail.comcorecrew.comcore.helpers;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single mention in a chat message.
 */
public abstract class ChatMention {
    private static final int MENTION_COLOR = Color.GREEN;

    /**
     * The index of the mention in the string.
     */
    public final int index;

    /**
     * Create a mention at a given index.
     *
     * @param index the index of the mention
     */
    private ChatMention(int index) {
        this.index = index;
    }

    /**
     * A mention of a user (starting with an at symbol).
     */
    public static class UserMention extends ChatMention {
        private static final String MENTION_ALL = "all";

        /**
         * The name that was mentioned.
         */
        public final String name;

        /**
         * Create a UserMention with a specific name.
         *
         * @param index the index of the mention
         * @param name  the name of the mention
         */
        private UserMention(int index, String name) {
            super(index);
            this.name = name;
        }

        @Override
        public final boolean mentionsUser(String name) {
            return this.name.equalsIgnoreCase(MENTION_ALL)
                    || this.name.equalsIgnoreCase(name);
        }

        @Override
        public final boolean shouldDisplayIn(Group group) {
            if (name.equalsIgnoreCase(MENTION_ALL)) {
                return true;
            }

            for (User user : group.getUsers()) {
                if (user.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int length() {
            return name.length() + 1;
        }

        @Override
        @NonNull
        public String toString() {
            return "@" + name;
        }
    }

    /**
     * A mention of a user surrounded by angle brackets.
     */
    private static class BlockMention extends UserMention {
        private static final char START_BLOCK = '<';
        private static final char END_BLOCK = '>';

        /**
         * Create a BlockMention with a specific name.
         *
         * @param index the index of the mention
         * @param name  the name of the mention
         */
        private BlockMention(int index, String name) {
            super(index, name);
        }

        @Override
        public int length() {
            return name.length() + 3;
        }
    }

    /**
     * A mention of an uploaded file.
     */
    public static final class UploadMention extends ChatMention {
        private static final Pattern REGEX = Pattern.compile("^https?://(?:www\\.)?" +
                "comcore\\.ml/file/[A-Za-z0-9]+-([a-zA-Z0-9._-]+)$");

        /**
         * The full URL of the mention.
         */
        public final String url;

        /**
         * Just the file name of the mention.
         */
        public final String fileName;

        /**
         * Create an UploadMention from a given URL.
         *
         * @param index    the index of the mention
         * @param url      the URL of the mention
         * @param fileName the file name of the mention
         */
        private UploadMention(int index, String url, String fileName) {
            super(index);
            this.url = url;
            this.fileName = fileName;
        }

        /**
         * Try to parse a file name from a URL.
         *
         * @param url the full URL
         * @return the file name if successful, otherwise null
         */
        public static String parseFileName(String url) {
            Matcher matcher = REGEX.matcher(url);
            if (matcher.matches()) {
                return matcher.group(1);
            } else {
                return null;
            }
        }

        @Override
        public ClickableSpan getClickableSpan() {
            return new URLSpan(url);
        }

        @Override
        public int length() {
            return url.length() + 3;
        }

        @Override
        @NonNull
        public String toString() {
            return fileName;
        }
    }

    /**
     * Check if a user is the one being mentioned (or "all").
     *
     * @param name the name of the user
     * @return true if they are mentioned, false otherwise
     */
    public boolean mentionsUser(String name) {
        return false;
    }

    /**
     * Check if the mention should be formatted specially in a group.
     *
     * @param group the group to check
     * @return true if the mention should be formatted, false otherwise
     */
    public boolean shouldDisplayIn(Group group) {
        return true;
    }

    /**
     * Create a clickable span for the mention.
     *
     * @return a clickable span (or null)
     */
    public ClickableSpan getClickableSpan() {
        return null;
    }

    /**
     * Get the number of characters in the mention.
     *
     * @return the number of characters in the mention
     */
    public abstract int length();

    /**
     * Parse the contents of a mention from a string.
     *
     * @param atIndex the index of the at symbol
     * @param message the message
     * @return the parsed ChatMention
     */
    private static ChatMention parseMention(int atIndex, String message) {
        // Start parsing after the at symbol
        int startIndex = atIndex + 1;
        if (startIndex >= message.length()) {
            return null;
        }

        // Check if this is a BLOCK style mention
        if (message.charAt(startIndex) == BlockMention.START_BLOCK) {
            // Parse the contents of the block mention
            int offset;
            for (offset = startIndex + 1; offset < message.length(); offset++) {
                char ch = message.charAt(offset);
                if (ch == BlockMention.END_BLOCK) {
                    // If the block ends, then it is valid
                    String name = message.substring(startIndex + 1, offset);

                    // Check for an upload mention
                    String fileName = UploadMention.parseFileName(name);
                    if (fileName != null) {
                        return new UploadMention(atIndex, name, fileName);
                    }

                    // Otherwise, return a plain block mention
                    return new BlockMention(atIndex, name);
                } else if (ch == '@' || ch == BlockMention.START_BLOCK) {
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
        return new UserMention(atIndex, name);
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
            ChatMention mention = parseMention(index, message);
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
     * @param message  the message to parse
     * @param group    the current group
     * @param mentions the mentions to format
     * @return the formatted string
     */
    public static CharSequence formatMentions(String message, Group group,
                                              List<ChatMention> mentions) {
        if (message == null || message.isEmpty()) {
            return "[deleted]";
        }

        if (mentions == null) {
            mentions = parseMentions(message);
        }

        // Update the text and add a span for each mention in reverse order
        SpannableStringBuilder builder = new SpannableStringBuilder(message);
        for (int i = mentions.size() - 1; i >= 0; i--) {
            ChatMention mention = mentions.get(i);
            // Only add the mention if they are part of the group
            if (mention.shouldDisplayIn(group)) {
                int start = mention.index;
                int end = start + mention.length();
                ClickableSpan clickable = mention.getClickableSpan();
                if (clickable != null) {
                    builder.setSpan(clickable, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                ForegroundColorSpan color = new ForegroundColorSpan(MENTION_COLOR);
                builder.setSpan(color, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                builder.replace(start, end, mention.toString());
            }
        }

        return builder;
    }
}
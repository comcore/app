package com.gmail.comcorecrew.comcore.classes;

import com.gmail.comcorecrew.comcore.enums.Reaction;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.util.Collections;
import java.util.Map;

public class ReactionHolder {

    private int[] reactions;

    /**
     * Creates a new reactionHolder from a cache string and a starting index
     *
     * @param base          array of characters to read from;
     * @param startIndex    index to start reading
     */
    public ReactionHolder(char[] base, int startIndex) {
        int length = base[startIndex++];
        length = base[startIndex++] | (length << 16);
        reactions = new int[length];
        for (int i = 0; i < length; i++) {
            reactions[i] = base[startIndex++];
            reactions[i] = base[startIndex++] | (reactions[i] << 16);
        }
    }

    /**
     * Creates a new reactionHolder from a cache string and a starting index
     *
     * @param base          array of characters to read from;
     * @param startIndex    index to start reading
     */
    public ReactionHolder(String base, int startIndex) {
        int length = base.charAt(startIndex++);
        length = base.charAt(startIndex++) | (length << 16);
        reactions = new int[length];
        for (int i = 0; i < length; i++) {
            reactions[i] = base.charAt(startIndex++);
            reactions[i] = base.charAt(startIndex++) | (reactions[i] << 16);
        }
    }

    public ReactionHolder() {
        this.reactions = Reaction.getEmptyReactionArray();
    }

    public ReactionHolder(int[] reactions) {
        this.reactions = reactions;
    }

    public ReactionHolder(int length) {
        this.reactions = new int[length];
    }

    public int[] getReactions() {
        return reactions;
    }

    public void setReactions(int[] reactions) {
        this.reactions = reactions;
    }

    public void resizeReactions(int length) {
        int[] newReactions = new int[length];

        for (int i = 0; i < Math.min(length, reactions.length); i++) {
            newReactions[i] = reactions[i];
        }

        reactions = newReactions;
    }

    public int getReactionCount(Reaction reaction) {
        return reactions[reaction.toInt() - 1];
    }

    public void addReaction(Reaction reaction) {
        reactions[reaction.toInt() - 1]++;
    }

    public void removeReaction(Reaction reaction) {
        reactions[reaction.toInt() - 1]--;
    }

    public void setReaction(Reaction reaction, int count) {
        reactions[reaction.toInt() - 1] = count;
    }

    public int getCharLength() {
        return 2 + 2 * reactions.length;
    }

    public Map<UserID, String> toReactionEntries() {
        // TODO decide whether this needs to be fixed
        return Collections.emptyMap();
    }
}

package com.gmail.comcorecrew.comcore.enums;

public enum Reaction {

    UNKNOWN,
    NONE,
    LIKE,
    DISLIKE;

    public static Reaction fromInt(int reaction) {
        switch (reaction) {
            case 0:
                return NONE;
            case 1:
                return LIKE;
            case 2:
                return DISLIKE;
            default:
                return UNKNOWN;
        }
    }

    public int toInt() {
        switch (this) {
            case NONE:
                return 0;
            case LIKE:
                return 1;
            case DISLIKE:
                return 2;
            default:
                return -1; //UNKNOWN
        }
    }

    public static Reaction[] reactions = {
            Reaction.LIKE,
            Reaction.DISLIKE,
    };

    public static int[] getEmptyReactionArray() {
        return new int[reactions.length];
    }

    public static Reaction fromString(String name) {
        if (name == null) {
            return Reaction.NONE;
        }

        for (Reaction reaction : reactions) {
            if (name.equalsIgnoreCase(reaction.name())) {
                return reaction;
            }
        }

        return Reaction.UNKNOWN;
    }
}

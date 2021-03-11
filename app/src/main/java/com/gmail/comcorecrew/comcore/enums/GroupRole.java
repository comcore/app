package com.gmail.comcorecrew.comcore.enums;

import androidx.annotation.NonNull;

/**
 * Represents the role of a user in a group which determines which actions they are able to do.
 */
public enum GroupRole {
    /**
     * A regular user who is a member of a group but has no special permissions.
     */
    USER,

    /**
     * A moderator who is able to change group details, assign roles, and mute users.
     */
    MODERATOR,

    /**
     * The owner of a group who has full permissions within the group.
     */
    OWNER;

    /**
     * Create a GroupRole from a String.
     *
     * @param role the data sent by the server
     * @return the GroupRole
     */
    public static GroupRole fromString(String role) {
        switch (role) {
            case "user":
                return USER;
            case "moderator":
                return MODERATOR;
            case "owner":
                return OWNER;
            default:
                throw new IllegalArgumentException("invalid GroupRole: " + role);
        }
    }

    @Override
    @NonNull
    public String toString() {
        switch (this) {
            case USER:
                return "user";
            case MODERATOR:
                return "moderator";
            case OWNER:
                return "owner";
            default:
                throw new IllegalStateException("invalid GroupRole");
        }
    }
}

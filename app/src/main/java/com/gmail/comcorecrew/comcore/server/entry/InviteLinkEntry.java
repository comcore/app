package com.gmail.comcorecrew.comcore.server.entry;

import com.google.gson.JsonObject;

/**
 * Represents the data associated with an invite link.
 */
public class InviteLinkEntry {
    /**
     * The invite link that can be used to join the group.
     */
    public final String inviteLink;

    /**
     * The name of the group associated with the invite link.
     */
    public final String groupName;

    /**
     * The timestamp after which the link will no longer be usable.
     */
    public final long expireTimestamp;

    /**
     * Create an InviteLinkEntry from an invite link, group name, and expiration timestamp.
     *
     * @param inviteLink      the invite link
     * @param groupName       the group name
     * @param expireTimestamp the expiration timestamp
     */
    public InviteLinkEntry(String inviteLink, String groupName, long expireTimestamp) {
        if (inviteLink == null || inviteLink.isEmpty()) {
            throw new IllegalArgumentException("invite link cannot be empty");
        } else if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("group name cannot be empty");
        } else if (expireTimestamp < 0) {
            throw new IllegalArgumentException("expire timestamp cannot be negative");
        }

        this.inviteLink = inviteLink;
        this.groupName = groupName;
        this.expireTimestamp = expireTimestamp;
    }

    /**
     * Create a InviteLinkEntry from a JsonObject. Returns null if the link is invalid.
     *
     * @param inviteLink the invite link
     * @param json       the data sent by the server
     * @return the InviteLinkEntry
     */
    public static InviteLinkEntry fromJson(String inviteLink, JsonObject json) {
        String name = json.get("name").getAsString();
        long expire = json.get("expire").getAsLong();
        return new InviteLinkEntry(inviteLink, name, expire);
    }

    /**
     * Check if the invite link has expired yet.
     *
     * @return true if the invite link has expired, false otherwise
     */
    public boolean hasExpired() {
        return expireTimestamp != 0 && System.currentTimeMillis() >= expireTimestamp;
    }
}
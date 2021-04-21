package com.gmail.comcorecrew.comcore.server.id;

import com.google.gson.JsonObject;

/**
 * Represents an identifier for a poll in a poll list.
 */
public class PollID extends ModuleItemID<PollListID> {
    /**
     * Create a PollID from a parent poll list and a numeric ID.
     *
     * @param pollList the parent poll list
     * @param id       the numeric ID
     */
    public PollID(PollListID pollList, long id) {
        super(pollList, id);
    }

    /**
     * Parse a PollID from a JsonObject. If specified, the given PollListID is used. Otherwise,
     * it is taken from the message data.
     *
     * @param pollList the poll list which contains the poll or null
     * @param json     the data sent by the server
     * @return the PollID
     */
    public static PollID fromJson(PollListID pollList, JsonObject json) {
        if (pollList == null) {
            GroupID group = new GroupID(json.get("group").getAsString());
            pollList = new PollListID(group, json.get("pollList").getAsString());
        }

        long id = json.get("id").getAsLong();
        return new PollID(pollList, id);
    }
}
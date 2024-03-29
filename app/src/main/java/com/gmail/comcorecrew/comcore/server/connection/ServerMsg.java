package com.gmail.comcorecrew.comcore.server.connection;

import com.google.gson.JsonObject;

/**
 * Represents a message to send or receive from the server.
 */
public final class ServerMsg {
    private static final String PING = "PING";

    /**
     * The kind of message which identifies the action to perform.
     */
    public final String kind;

    /**
     * The data of the message containing additional information.
     */
    public final JsonObject data;

    /**
     * Create a message that will succeed immediately with no data.
     */
    public ServerMsg() {
        this(PING);
    }

    /**
     * Create a message from just the kind, with no additional data.
     *
     * @param kind the kind of message
     */
    public ServerMsg(String kind) {
        this(kind, new JsonObject());
    }

    /**
     * Create a message from a kind and some data to send.
     *
     * @param kind the kind of message
     * @param data the data of the message
     */
    public ServerMsg(String kind, JsonObject data) {
        if (kind == null || kind.isEmpty()) {
            throw new IllegalArgumentException("message kind cannot be null or empty");
        } else if (data == null) {
            throw new IllegalArgumentException("message data cannot be null");
        }

        this.kind = kind;
        this.data = data;
    }

    /**
     * Check if this message is a PING message.
     *
     * @return true if this is a PING message, false otherwise
     */
    public boolean isPing() {
        return kind.equals(PING);
    }

    /**
     * Convert the message to a JsonObject to be sent.
     *
     * @return the message as a JsonObject
     */
    public JsonObject toJson() {
        JsonObject message = new JsonObject();
        message.addProperty("kind", kind);
        message.add("data", data);
        return message;
    }

    /**
     * Parse a Message from a JsonObject.
     *
     * @param json the data sent by the server
     * @return the Message
     */
    public static ServerMsg fromJson(JsonObject json) {
        String kind = json.get("kind").getAsString();
        JsonObject data = json.getAsJsonObject("data");
        return new ServerMsg(kind, data);
    }
}
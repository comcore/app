package com.gmail.comcorecrew.comcore.server.connection;

import com.google.gson.JsonObject;

/**
 * Represents a message to send or receive from the server.
 */
public final class ServerMsg {
    /**
     * The kind of message which identifies the action to perform.
     */
    public final String kind;

    /**
     * The data of the message containing additional information.
     */
    public final JsonObject data;

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
        JsonObject data = json.get("data").getAsJsonObject();
        return new ServerMsg(kind, data);
    }
}
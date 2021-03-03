package com.gmail.comcorecrew.comcore.server.connection;

import com.google.gson.JsonObject;

/**
 * Represents a message to send or receive from the server.
 */
public class Message {
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
    public Message(String kind) {
        this(kind, new JsonObject());
    }

    /**
     * Create a message from a kind and some data to send.
     *
     * @param kind the kind of message
     * @param data the data of the message
     */
    public Message(String kind, JsonObject data) {
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
     * Parse a message from a JsonObject.
     *
     * @param json the json containing the message
     * @return the parsed message
     */
    public static Message fromJson(JsonObject json) {
        String kind = json.get("kind").getAsString();
        JsonObject data = json.get("data").getAsJsonObject();
        return new Message(kind, data);
    }
}
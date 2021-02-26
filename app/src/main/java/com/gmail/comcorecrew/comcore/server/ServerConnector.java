package com.gmail.comcorecrew.comcore.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Singleton class representing a connection with the server.
 */
public abstract class ServerConnector {
    private static ServerConnector INSTANCE;

    /**
     * Set the instance which will be used for all server requests. This could be used to set a
     * MockConnector for testing purposes before making a request.
     *
     * @param connector the ServerConnector instance to use for requests
     */
    public static void setInstance(ServerConnector connector) {
        if (INSTANCE != null) {
            INSTANCE.stop();
        }
        INSTANCE = connector;
    }

    /**
     * Get the ServerConnector instance which should be used for server requests.
     *
     * @return the ServerConnector instance to use for requests
     */
    private static ServerConnector getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerConnectorImpl();
        }
        return INSTANCE;
    }

    /**
     * Close the connection to the server.
     */
    protected void stop() {}

    /**
     * Send a message to the server and wait for a response synchronously.
     *
     * @param kind    the kind of message to send to the server
     * @param message the contents of the message to send to the server (may be null)
     * @return a ServerResult containing the response of the server
     */
    protected abstract ServerResult<JsonObject> sendSync(String kind, JsonObject message);

    /**
     * Send a message to the server and handle the result asynchronously when it arrives.
     * If the handler is null, the server response will be ignored.
     *
     * @param kind    the kind of message to send to the server
     * @param message the contents of the message to send to the server (may be null)
     * @param handler the handler for the response of the server
     */
    protected void sendAsync(String kind, JsonObject message, ResultHandler<JsonObject> handler) {
        ServerResult<JsonObject> result = sendSync(kind, message);
        if (handler != null) {
            handler.handleResult(result);
        }
    }

    /**
     * Create a new group with a given name.
     *
     * @param name the name of the group
     * @return the GroupID of the created group
     */
    public static ServerResult<GroupID> createGroup(String name) {
        JsonObject message = new JsonObject();
        message.addProperty("name", name);
        return getInstance().sendSync("createGroup", message).tryMap(response ->
            new GroupID(response.get("uuid").getAsString())
        );
    }

    /**
     * Get a list of all groups that the user is in.
     *
     * @return an array with each group's GroupID
     */
    public static ServerResult<GroupID[]> getGroups() {
        return getInstance().sendSync("getGroups", null).tryMap(response -> {
            JsonArray groupsJson = response.get("groups").getAsJsonArray();
            GroupID[] groups = new GroupID[groupsJson.size()];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = new GroupID(groupsJson.get(i).getAsString());
            }
            return groups;
        });
    }

    /**
     * Create a new chat with a given name.
     *
     * @param group the parent group of the chat
     * @param name  the name of the chat
     * @return the ChatID of the created chat
     */
    public static ServerResult<ChatID> createChat(GroupID group, String name) {
        JsonObject message = new JsonObject();
        message.addProperty("group", group.uuid.toString());
        message.addProperty("name", name);
        return getInstance().sendSync("createChat", message).tryMap(response ->
                new ChatID(group, response.get("uuid").getAsString())
        );
    }

    /**
     * Get a list of all chats in a group.
     *
     * @param group the group to list the chats of
     * @return an array with each chat's ChatID
     */
    public static ServerResult<ChatID[]> getChats(GroupID group) {
        JsonObject message = new JsonObject();
        message.addProperty("group", group.uuid.toString());
        return getInstance().sendSync("getChats", message).tryMap(response -> {
            JsonArray chatsJson = response.get("chats").getAsJsonArray();
            ChatID[] chats = new ChatID[chatsJson.size()];
            for (int i = 0; i < chats.length; i++) {
                chats[i] = new ChatID(group, chatsJson.get(i).getAsString());
            }
            return chats;
        });
    }

    // TODO add methods for more request types

    // TODO add handling for receiving notifications from the server
}
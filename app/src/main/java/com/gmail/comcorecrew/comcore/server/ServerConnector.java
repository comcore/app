package com.gmail.comcorecrew.comcore.server;

import com.gmail.comcorecrew.comcore.server.entry.ChatEntry;
import com.gmail.comcorecrew.comcore.server.entry.GroupEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
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
            throw new IllegalStateException("ServerConnector not initialized!");
        }
        return INSTANCE;
    }

    /**
     * Close the connection to the server.
     */
    protected void stop() {}

    /**
     * Request the server to authenticate the user. This should be called before making any requests
     * from the server. If this method is not called, all requests will likely fail.
     *
     * @param email   the user's email address
     * @param pass    the user's password
     * @param handler the handler for the response of the server
     */
    protected abstract void authenticate(String email, String pass, ResultHandler<Void> handler);

    /**
     * Send a message to the server and handle the result asynchronously when it arrives.
     * If the handler is null, the server response will be ignored.
     *
     * @param kind     the kind of message to send to the server
     * @param data     the contents of the message to send to the server
     * @param handler  the handler for the response of the server
     * @param function a function to convert the raw response to the proper type
     */
    protected abstract <T> void send(String kind, JsonObject data,
                                     ResultHandler<T> handler,
                                     ServerResult.Function<JsonObject, T> function);

    /**
     * Create a new group with a given name.
     *
     * @param name the name of the group
     * @return the GroupID of the created group
     */
    public static void createGroup(String name, ResultHandler<GroupID> handler) {
        JsonObject message = new JsonObject();
        message.addProperty("name", name);
        getInstance().send("createGroup", message, handler,
                response -> new GroupID(response.get("uuid").getAsString()));
    }

    /**
     * Get a list of all groups that the user is in.
     *
     * @param handler the handler for the response of the server
     */
    public static void getGroups(ResultHandler<GroupEntry[]> handler) {
        getInstance().send("getGroups", null, handler, response -> {
            JsonArray groupsJson = response.get("groups").getAsJsonArray();
            GroupEntry[] groups = new GroupEntry[groupsJson.size()];
            for (int i = 0; i < groups.length; i++) {
                JsonObject group = groupsJson.get(i).getAsJsonObject();
                GroupID id = new GroupID(group.get("id").getAsString());
                String name = group.get("name").getAsString();
                groups[i] = new GroupEntry(id, name);
            }
            return groups;
        });
    }

    /**
     * Create a new chat with a given name.
     *
     * @param group   the parent group of the chat
     * @param name    the name of the chat
     * @param handler the handler for the response of the server
     */
    public static void createChat(GroupID group, String name, ResultHandler<ChatID> handler) {
        JsonObject message = new JsonObject();
        message.addProperty("group", group.uuid.toString());
        message.addProperty("name", name);
        getInstance().send("createChat", message, handler, response ->
                new ChatID(group, response.get("uuid").getAsString())
        );
    }

    /**
     * Get a list of all chats in a group.
     *
     * @param group   the group to list the chats of
     * @param handler the handler for the response of the server
     */
    public static void getChats(GroupID group, ResultHandler<ChatEntry[]> handler) {
        JsonObject message = new JsonObject();
        message.addProperty("group", group.uuid.toString());
        getInstance().send("getChats", message, handler, response -> {
            JsonArray chatsJson = response.get("chats").getAsJsonArray();
            ChatEntry[] chats = new ChatEntry[chatsJson.size()];
            for (int i = 0; i < chats.length; i++) {
                JsonObject chat = chatsJson.get(i).getAsJsonObject();
                ChatID id = new ChatID(group, chat.get("id").getAsString());
                String name = chat.get("name").getAsString();
                chats[i] = new ChatEntry(id, name);
            }
            return chats;
        });
    }

    // TODO add methods for more request types

    // TODO add handling for receiving notifications from the server
}
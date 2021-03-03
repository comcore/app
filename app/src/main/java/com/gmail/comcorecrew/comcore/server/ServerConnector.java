package com.gmail.comcorecrew.comcore.server;

import com.gmail.comcorecrew.comcore.server.connection.Connection;
import com.gmail.comcorecrew.comcore.server.connection.Message;
import com.gmail.comcorecrew.comcore.server.entry.ChatEntry;
import com.gmail.comcorecrew.comcore.server.entry.GroupEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Utility class representing a connection with the server.
 */
public final class ServerConnector {
    private static Connection serverConnection;

    /**
     * Set the connection which will be used for all server requests.
     *
     * @param connection the connection to use for requests
     */
    public static void setConnection(Connection connection) {
        if (serverConnection != null) {
            serverConnection.stop();
        }
        serverConnection = connection;
    }

    /**
     * Get the connection which should be used for server requests.
     *
     * @return the connection to use for requests
     */
    private static Connection getConnection() {
        if (serverConnection == null) {
            throw new IllegalStateException("ServerConnector not initialized!");
        }
        return serverConnection;
    }

    /**
     * Request the server to authenticate the user. This should be called before making any requests
     * from the server. If this method is not called, all requests will likely fail.
     *
     * @param email   the user's email address
     * @param pass    the user's password
     * @param handler the handler for the response of the server
     */
    public static void authenticate(String email, String pass, ResultHandler<Void> handler) {
        getConnection().authenticate(email, pass, handler);
    }

    /**
     * Create a new group with a given name.
     *
     * @param name the name of the group
     * @param handler the handler for the response of the server
     */
    public static void createGroup(String name, ResultHandler<GroupID> handler) {
        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        getConnection().send(new Message("createGroup", data), handler,
                response -> new GroupID(response.get("id").getAsString()));
    }

    /**
     * Get a list of all groups that the user is in.
     *
     * @param handler the handler for the response of the server
     */
    public static void getGroups(ResultHandler<GroupEntry[]> handler) {
        getConnection().send(new Message("getGroups"), handler, response -> {
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
        JsonObject data = new JsonObject();
        data.addProperty("group", group.uuid.toString());
        data.addProperty("name", name);
        getConnection().send(new Message("createChat", data), handler, response ->
                new ChatID(group, response.get("id").getAsString())
        );
    }

    /**
     * Get a list of all chats in a group.
     *
     * @param group   the group to list the chats of
     * @param handler the handler for the response of the server
     */
    public static void getChats(GroupID group, ResultHandler<ChatEntry[]> handler) {
        JsonObject data = new JsonObject();
        data.addProperty("group", group.uuid.toString());
        getConnection().send(new Message("getChats", data), handler, response -> {
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
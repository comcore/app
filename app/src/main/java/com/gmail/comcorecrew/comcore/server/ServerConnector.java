package com.gmail.comcorecrew.comcore.server;

import com.gmail.comcorecrew.comcore.server.connection.Connection;
import com.gmail.comcorecrew.comcore.server.connection.Message;
import com.gmail.comcorecrew.comcore.server.entry.*;
import com.gmail.comcorecrew.comcore.server.id.*;
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
     * @throws IllegalStateException if the connection hasn't been initialized
     */
    public static Connection getConnection() {
        if (serverConnection == null) {
            throw new IllegalStateException("ServerConnector not initialized!");
        }

        return serverConnection;
    }

    /**
     * Request the server to authenticate the user. This should be called before making any requests
     * from the server. If this method is not called, all requests will likely fail.
     *
     * @param email         the user's email address
     * @param pass          the user's password
     * @param createAccount true if creating an account, false if logging into an existing account
     * @param handler       the handler for the response of the server
     */
    public static void authenticate(String email, String pass, boolean createAccount,
                                    ResultHandler<LoginStatus> handler) {
        if (email == null || pass == null) {
            throw new IllegalArgumentException("email address and password cannot be null");
        }

        getConnection().authenticate(email, pass, createAccount, handler);
    }

    /**
     * Enter a code to confirm an email address after receiving LoginStatus.ENTER.CODE. Returns
     * true if the code was correct, false otherwise.
     *
     * @param code    the code the user entered
     * @param handler the handler for the response of the server
     */
    public static void enterCode(String code, ResultHandler<Boolean> handler) {
        if (code == null) {
            throw new IllegalArgumentException("code cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("code", code);
        getConnection().send(new Message("enterCode", data), handler, response ->
            response.get("correct").getAsBoolean());
    }

    /**
     * Create a new group with a given name.
     *
     * @param name the name of the group
     * @param handler the handler for the response of the server
     */
    public static void createGroup(String name, ResultHandler<GroupID> handler) {
        if (name == null) {
            throw new IllegalArgumentException("group name cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        getConnection().send(new Message("createGroup", data), handler, response ->
                new GroupID(response.get("id").getAsString()));
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
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (name == null) {
            throw new IllegalArgumentException("chat name cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("name", name);
        getConnection().send(new Message("createChat", data), handler, response ->
                new ChatID(group, response.get("id").getAsString())
        );
    }

    /**
     * Get a list of all users in a group.
     *
     * @param group   the group to list the users of
     * @param handler the handler for the response of the server
     */
    public static void getUsers(GroupID group, ResultHandler<UserEntry[]> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        getConnection().send(new Message("getUsers", data), handler, response -> {
            JsonArray usersJson = response.get("users").getAsJsonArray();
            UserEntry[] users = new UserEntry[usersJson.size()];
            for (int i = 0; i < users.length; i++) {
                users[i] = UserEntry.fromJson(usersJson.get(i).getAsJsonObject());
            }
            return users;
        });
    }

    /**
     * Get a list of all chats in a group.
     *
     * @param group   the group to list the chats of
     * @param handler the handler for the response of the server
     */
    public static void getChats(GroupID group, ResultHandler<ChatEntry[]> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
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

    /**
     * Send a message in a chat.
     *
     * @param chat    the chat to send the message in
     * @param message the message to send
     * @param handler the handler for the response of the server
     */
    public static void sendMessage(ChatID chat, String message, ResultHandler<Void> handler) {
        if (chat == null) {
            throw new IllegalArgumentException("ChatID cannot be null");
        } else if (message == null) {
            throw new IllegalArgumentException("chat message cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", chat.group.id);
        data.addProperty("chat", chat.id);
        data.addProperty("message", message);
        getConnection().send(new Message("sendMessage", data), handler, response -> null);
    }

    /**
     * Get messages in a chat. The server will return the most recent messages which were sent after
     * timestampAfter but before timestampBefore. If either bound is 0, it is ignored, so (0, X)
     * returns messages before X while (X, 0) returns messages after X, where X is some timestamp.
     *
     * This interface allows the app to fetch only messages which have arrived since it was last
     * refreshed by passing (lastRefreshTime, 0), or to fetch an older set of messages when
     * scrolling backwards by passing (0, oldestCachedMessageTimestamp). Passing (0, 0) will place
     * no limits on the times the messages were received.
     *
     * The server places a limit on how many messages it will return per request, so it is not
     * guaranteed to be an exhaustive list of the messages between the timestamps. The lower bound
     * is inclusive and the upper bound is exclusive.
     *
     * @param chat            the chat to request messages from
     * @param timestampAfter  a lower bound on the timestamp, or 0 if no lower bound
     * @param timestampBefore an upper bound on the timestamp, or 0 if no upper bound
     * @param handler         the handler for the response of the server
     */
    public static void getMessages(ChatID chat, long timestampAfter, long timestampBefore,
                                   ResultHandler<MessageEntry[]> handler) {
        if (chat == null) {
            throw new IllegalArgumentException("ChatID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", chat.group.id);
        data.addProperty("chat", chat.id);
        data.addProperty("timestampAfter", timestampAfter);
        data.addProperty("timestampBefore", timestampBefore);
        getConnection().send(new Message("getMessages", data), handler, response -> {
            JsonArray messagesJson = response.get("messages").getAsJsonArray();
            MessageEntry[] messages = new MessageEntry[messagesJson.size()];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = MessageEntry.fromJson(messagesJson.get(i).getAsJsonObject());
            }
            return messages;
        });
    }
}
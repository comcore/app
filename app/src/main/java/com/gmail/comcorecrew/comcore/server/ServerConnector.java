package com.gmail.comcorecrew.comcore.server;

import android.os.Handler;
import android.os.Looper;

import com.gmail.comcorecrew.comcore.server.connection.Connection;
import com.gmail.comcorecrew.comcore.server.connection.Function;
import com.gmail.comcorecrew.comcore.server.connection.Message;
import com.gmail.comcorecrew.comcore.server.entry.*;
import com.gmail.comcorecrew.comcore.server.id.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class representing a connection with the server.
 */
public final class ServerConnector {
    private static final List<NotificationListener> notificationListeners =
            Collections.synchronizedList(new ArrayList<>());
    private static Connection serverConnection;

    /**
     * Set the connection which will be used for all server requests.
     *
     * @param connection the connection to use for requests
     * @see Connection
     */
    public static void setConnection(Connection connection) {
        if (connection == serverConnection) {
            return;
        }

        if (serverConnection != null) {
            // Stop the connection using a different thread
            new Thread(serverConnection::stop, "ServerConnection.stop()").start();
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
     * Add a NotificationListener to the ServerConnector.
     *
     * @param listener the NotificationListener to add
     * @see NotificationListener
     */
    public static void addNotificationListener(NotificationListener listener) {
        notificationListeners.add(listener);
    }

    /**
     * Call a function for all notification listeners attached to the ServerConnector. The listeners
     * are called in the order they are added, and if the function returns true, iteration will
     * stop immediately. The notification listeners are always called on the main thread.
     *
     * @param function the function to call
     */
    public static void foreachListener(Function<NotificationListener, Boolean> function) {
        new Handler(Looper.getMainLooper()).post(() -> {
            synchronized (notificationListeners) {
                for (NotificationListener listener : notificationListeners) {
                    try {
                        if (function.apply(listener)) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Log out if logged in. It will be necessary to call authenticate() again.
     */
    public static void logout() {
        getConnection().logout();
    }

    /**
     * Request the server to authenticate the user. This should be called before making any requests
     * from the server. If this method is not called, all requests will likely fail.
     *
     * If createAccount is true, an account will be created for the user. LoginStatus.ENTER_CODE
     * will be returned if successful, otherwise LoginStatus.ALREADY_EXISTS will be returned if the
     * email address is already in use.
     *
     * If requestReset() and enterCode() have been successfully called, the user's password will be
     * reset to the provided password and they will be signed in.
     *
     * Otherwise, the password will be checked against the account on the server.
     *
     * @param email         the user's email address
     * @param pass          the user's password
     * @param createAccount true if creating an account, false if logging into an existing account
     * @param handler       the handler for the response of the server
     * @see LoginStatus
     */
    public static void authenticate(String email, String pass, boolean createAccount,
                                    ResultHandler<LoginStatus> handler) {
        if (email == null || pass == null) {
            throw new IllegalArgumentException("email address and password cannot be null");
        }

        getConnection().authenticate(email, pass, createAccount, handler);
    }

    /**
     * Request a code to be sent to the user's email address so they can reset their password.
     * Returns true if the code was sent and false if the account does not exist.
     *
     * @param email   the user's email address
     * @param handler the handler for the response of the server
     */
    public static void requestReset(String email, ResultHandler<Boolean> handler) {
        if (email == null) {
            throw new IllegalArgumentException("email address cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("email", email);
        getConnection().send(new Message("requestReset", data), handler, response ->
                response.get("sent").getAsBoolean());
    }

    /**
     * Enter a code to confirm an email address after receiving LoginStatus.ENTER_CODE from
     * authenticate() or to reset a password after calling requestCode(). Returns true if the code
     * was correct and false otherwise.
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
     * @see GroupEntry
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
     * @see UserEntry
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
     * @see ChatEntry
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
        data.addProperty("contents", message);
        getConnection().send(new Message("sendMessage", data), handler, response -> null);
    }

    /**
     * Get messages in a chat. The server will only return a limited number of messages, and the
     * messages will always be the most recent messages in the requested interval of message IDs.
     *
     * @param chat    the chat to request messages from
     * @param after   if not null, only request messages sent after this message
     * @param before  if not null, only request messages sent before this message
     * @param handler the handler for the response of the server
     * @see MessageEntry
     */
    public static void getMessages(ChatID chat, MessageID after, MessageID before,
                                   ResultHandler<MessageEntry[]> handler) {
        if (chat == null) {
            throw new IllegalArgumentException("ChatID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", chat.group.id);
        data.addProperty("chat", chat.id);
        data.addProperty("after", after == null ? 0 : after.id);
        data.addProperty("before", before == null ? 0 : before.id);
        getConnection().send(new Message("getMessages", data), handler, response -> {
            JsonArray messagesJson = response.get("messages").getAsJsonArray();
            MessageEntry[] messages = new MessageEntry[messagesJson.size()];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = MessageEntry.fromJson(chat, messagesJson.get(i).getAsJsonObject());
            }
            return messages;
        });
    }
}
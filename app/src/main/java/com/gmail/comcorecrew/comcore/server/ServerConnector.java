package com.gmail.comcorecrew.comcore.server;

import android.os.Handler;
import android.os.Looper;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
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
     * Get the information of the currently logged in user.
     *
     * @return the user data or null if there is no logged in user
     */
    public static UserEntry getUser() {
        return getConnection().getUser();
    }

    /**
     * Log into the server using the user's information. This should be called before making any
     * requests from the server. If this method is not called, all requests will likely fail.
     *
     * @param email   the user's email address
     * @param pass    the user's password
     * @param handler the handler for the response of the server
     * @see LoginStatus
     */
    public static void login(String email, String pass, ResultHandler<LoginStatus> handler) {
        if (email == null || pass == null) {
            throw new IllegalArgumentException("email address and password cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("email", email);
        data.addProperty("pass", pass);
        Connection connection = getConnection();
        connection.send(new Message("login", data), handler, response -> {
            LoginStatus status = LoginStatus.fromJson(response);
            if (status.isValid) {
                connection.setInformation(email, pass);
            }
            return status;
        });
    }

    /**
     * Create a new account with the specified details. Returns true if the account was created
     * and false if an account with the email already exists. After creating an account, it will
     * be necessary to enter a code sent to the user's email address.
     *
     * @param name    the user's name
     * @param email   the user's email address
     * @param pass    the user's password
     * @param handler the handler for the response of the server
     */
    public static void createAccount(String name, String email, String pass,
                                     ResultHandler<Boolean> handler) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        } else if (email == null || pass == null) {
            throw new IllegalArgumentException("email address and password cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        data.addProperty("email", email);
        data.addProperty("pass", pass);
        Connection connection = getConnection();
        connection.send(new Message("createAccount", data), handler, response -> {
            boolean created = response.get("created").getAsBoolean();
            if (created) {
                connection.setInformation(email, pass);
            }
            return created;
        });
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
        Connection connection = getConnection();
        connection.send(new Message("requestReset", data), handler, response -> {
            boolean sent = response.get("sent").getAsBoolean();
            if (sent) {
                connection.setInformation(email, null);
            }
            return sent;
        });
    }

    /**
     * Enter a code to confirm an email address. This request is used for creating an account,
     * resetting a password, and two-factor authentication. Returns true if the code was correct
     * and false otherwise.
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
     * Finish resetting the user's password. The user must have entered the code sent to their email
     * before this method is called.
     *
     * @param pass    the user's new password
     * @param handler the handler for the response of the server
     */
    public static void finishReset(String pass, ResultHandler<Void> handler) {
        if (pass == null) {
            throw new IllegalArgumentException("password cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("pass", pass);
        Connection connection = getConnection();
        connection.send(new Message("finishReset", data), handler, response -> {
            connection.setInformation(null, pass);
            return null;
        });
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
                groups[i] = GroupEntry.fromJson(groupsJson.get(i).getAsJsonObject());
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
     * Get a list of all users in a group, including the current user.
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
     * Send an invite to a user with the corresponding email address to join the group. The request
     * will fail if the user doesn't have the ability to send invites in a group. Returns true if
     * the invite was sent and false if there was no user with the email address.
     *
     * @param group   the group to send the invite for
     * @param email   the email address of the target user
     * @param handler the handler for the response of the server
     */
    public static void sendInvite(GroupID group, String email, ResultHandler<Boolean> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (email == null) {
            throw new IllegalArgumentException("email address cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("email", email);
        getConnection().send(new Message("sendInvite", data), handler, response ->
                response.get("sent").getAsBoolean());
    }

    /**
     * Get a list of groups that the user has been invited to join.
     *
     * @param handler the handler for the response of the server
     * @see GroupInviteEntry
     */
    public static void getInvites(ResultHandler<GroupInviteEntry[]> handler) {
        getConnection().send(new Message("getInvites"), handler, response -> {
            JsonArray invitesJson = response.get("invites").getAsJsonArray();
            GroupInviteEntry[] invites = new GroupInviteEntry[invitesJson.size()];
            for (int i = 0; i < invites.length; i++) {
                invites[i] = GroupInviteEntry.fromJson(invitesJson.get(i).getAsJsonObject());
            }
            return invites;
        });
    }

    /**
     * Reply to an invitation to join a group, removing it from the list of pending invites.
     *
     * @param group   the group to accept/decline the invitation for
     * @param accept  whether the user wants to accept or decline the invitation
     * @param handler the handler for the response of the server
     */
    public static void replyToInvite(GroupID group, boolean accept, ResultHandler<Void> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("accept", accept);
        getConnection().send(new Message("replyToInvite", data), handler, response -> null);
    }

    /**
     * Leave a group.
     *
     * @param group   the group to leave
     * @param handler the handler for the response of the server
     */
    public static void leaveGroup(GroupID group, ResultHandler<Void> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        getConnection().send(new Message("leaveGroup", data), handler, response -> null);
    }

    /**
     * Kick the target user from a group that the user is a moderator or the owner of. The request
     * will fail if the user doesn't have the ability to kick people.
     *
     * @param group   the group to kick someone from
     * @param target  the user to kick from the group
     * @param handler the handler for the response of the server
     */
    public static void kick(GroupID group, UserID target, ResultHandler<Void> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (target == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("target", target.id);
        getConnection().send(new Message("kick", data), handler, response -> null);
    }

    /**
     * Set the role of a target user in a group that the user is a moderator or the owner of. The
     * request will fail if the user doesn't have the ability to change the role. If the owner sets
     * another user to be the owner, they will no longer be the owner afterwards since a group can
     * only have one owner.
     *
     * @param group   the group to set the role in
     * @param target  the user to set the role of
     * @param role    the role
     * @param handler the handler for the response of the server
     * @see GroupRole
     */
    public static void setRole(GroupID group, UserID target, GroupRole role,
                               ResultHandler<Void> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (target == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        } else if (role == null) {
            throw new IllegalArgumentException("GroupRole cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("target", target.id);
        data.addProperty("role", role.toString());
        getConnection().send(new Message("setRole", data), handler, response -> null);
    }

    /**
     * Set the muted status of a target user in a group that the user is a moderator or the owner
     * of. The request will fail if the user doesn't have the ability to mute the user.
     *
     * @param group   the group to set the role in
     * @param target  the user to set the role of
     * @param muted   whether the target user should be muted
     * @param handler the handler for the response of the server
     */
    public static void setMuted(GroupID group, UserID target, boolean muted,
                                ResultHandler<Void> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (target == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("target", target.id);
        data.addProperty("muted", muted);
        getConnection().send(new Message("setMuted", data), handler, response -> null);
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
        } else if (after != null && after.chat != chat) {
            throw new IllegalArgumentException("'after' message must be in the same chat");
        } else if (before != null && before.chat != chat) {
            throw new IllegalArgumentException("'before' message must be in the same chat");
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
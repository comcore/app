package com.gmail.comcorecrew.comcore.server;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.Reaction;
import com.gmail.comcorecrew.comcore.enums.TaskStatus;
import com.gmail.comcorecrew.comcore.notifications.NotificationListener;
import com.gmail.comcorecrew.comcore.server.connection.Connection;
import com.gmail.comcorecrew.comcore.server.connection.Function;
import com.gmail.comcorecrew.comcore.server.connection.ServerMsg;
import com.gmail.comcorecrew.comcore.server.entry.*;
import com.gmail.comcorecrew.comcore.server.id.*;
import com.gmail.comcorecrew.comcore.server.info.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class representing a connection with the server.
 */
public final class ServerConnector {
    private static Connection serverConnection;
    private static NotificationListener notificationListener;

    private ServerConnector() {}

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
     * Set a base NotificationListener for the ServerConnector.
     *
     * @param listener the NotificationListener to set
     * @see NotificationListener
     */
    public static void setNotificationListener(NotificationListener listener) {
        notificationListener = listener;
    }

    /**
     * Represents a notification to send to a notification listener.
     *
     * @see NotificationListener
     */
    public interface Notification {
        void notify(NotificationListener listener);
    }

    /**
     * Pass a notification to all of the notification listeners attached to the ServerConnector.
     * The listeners are always notified on the main thread. Parents are always notified before
     * their children, but that is the only constraint on the order.
     *
     * @param notification the notification to send
     * @see NotificationListener
     * @see Notification
     */
    public static void sendNotification(Notification notification) {
        // Run on the main thread
        new Handler(Looper.getMainLooper()).post(() -> {
            // Create a queue from the base notification listener
            ArrayDeque<NotificationListener> listeners = new ArrayDeque<>();
            listeners.add(notificationListener);

            // Loop over the notification listeners, giving each the notification
            while (!listeners.isEmpty()) {
                try {
                    NotificationListener listener = listeners.removeLast();
                    notification.notify(listener);

                    // Visit all of the children next, in undefined order
                    Collection<? extends NotificationListener> children = listener.getChildren();
                    if (children != null) {
                        listeners.addAll(children);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Do something after every other action has finished.
     *
     * @param callback what to do next
     */
    public static void then(Runnable callback) {
        getConnection().send(new ServerMsg("PING"), result -> callback.run(), response -> null);
    }

    /**
     * Log out if logged in. It will be necessary to call login() again.
     */
    public static void logout() {
        getConnection().logout();
    }

    /**
     * Connect to the server using the given LoginToken. If the LoginToken becomes invalid, then
     * NotificationListener.onLoggedOut() will be called.
     *
     * @param token the LoginToken to connect with
     */
    public static void connect(LoginToken token) {
        if (token == null) {
            throw new IllegalArgumentException("LoginToken cannot be null");
        }

        getConnection().connect(token);
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
        connection.send(new ServerMsg("login", data), handler, response -> {
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
        connection.send(new ServerMsg("createAccount", data), handler, response -> {
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
        connection.send(new ServerMsg("requestReset", data), handler, response -> {
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
        getConnection().send(new ServerMsg("enterCode", data), handler, response ->
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
        connection.send(new ServerMsg("finishReset", data), handler, response -> {
            connection.setInformation(null, pass);
            return null;
        });
    }

    /**
     * Checks if two-factor authentication is enabled for the current user. If two-factor
     * authentication is enabled, all login requests will return LoginStatus.ENTER_CODE rather than
     * LoginStatus.SUCCESS, and the user will only be able to log in after entering the code.
     *
     * @param handler the handler for the response of the server
     */
    public static void getTwoFactor(ResultHandler<Boolean> handler) {
        getConnection().send(new ServerMsg("getTwoFactor"), handler, response ->
                response.get("enabled").getAsBoolean());
    }

    /**
     * Enables or disables two-factor authentication for the user.
     *
     * @param handler the handler for the response of the server
     */
    public static void setTwoFactor(boolean enabled, ResultHandler<Void> handler) {
        JsonObject data = new JsonObject();
        data.addProperty("enabled", enabled);
        getConnection().send(new ServerMsg("setTwoFactor", data), handler, response -> null);
    }

    /**
     * Create a new group with a given name.
     *
     * @param name    the name of the group
     * @param handler the handler for the response of the server
     */
    public static void createGroup(String name, ResultHandler<GroupID> handler) {
        if (name == null) {
            throw new IllegalArgumentException("group name cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        getConnection().send(new ServerMsg("createGroup", data), handler, response ->
                new GroupID(response.get("id").getAsString()));
    }

    /**
     * Create a sub-group with members from a specific group. If any users are not in the group,
     * they will not be added to the sub-group. The current user will be added regardless of whether
     * they are selected in the list.
     *
     * @param group   the group to create a sub-group from
     * @param name    the name of the sub-group
     * @param users   the users to put in the sub-group
     * @param handler the handler for the response of the server
     */
    public static void createSubGroup(GroupID group, String name, Collection<UserID> users,
                                      ResultHandler<GroupID> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (users == null) {
            throw new IllegalArgumentException("user list cannot be null");
        } else if (name == null) {
            throw new IllegalArgumentException("sub-group name cannot be null");
        }

        JsonArray array = new JsonArray();
        for (UserID user : users) {
            if (user == null) {
                throw new IllegalArgumentException("UserID cannot be null");
            }
            array.add(user.toJson());
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("name", name);
        data.add("users", array);
        getConnection().send(new ServerMsg("createSubGroup", data), handler, response ->
                new GroupID(response.get("id").getAsString()));
    }

    /**
     * Create a direct messaging group with another user.
     *
     * @param target  the user to create the direct messaging group with
     * @param handler the handler for the response of the server
     */
    public static void createDirectMessage(UserID target, ResultHandler<GroupID> handler) {
        if (target == null) {
            throw new IllegalArgumentException("UserID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("target", target.id);
        getConnection().send(new ServerMsg("createDirectMessage", data), handler, response ->
                new GroupID(response.get("id").getAsString()));
    }

    /**
     * Get a list of all groups that the user is in.
     *
     * @param handler the handler for the response of the server
     */
    public static void getGroups(ResultHandler<GroupID[]> handler) {
        requestArray(new ServerMsg("getGroups"), handler, GroupID.class,
                "groups", json -> new GroupID(json.get("id").getAsString()));
    }

    /**
     * Create a new chat with a given name.
     *
     * @param group   the parent group of the chat
     * @param name    the name of the chat
     * @param handler the handler for the response of the server
     */
    public static void createChat(GroupID group, String name, ResultHandler<ChatID> handler) {
        createModule("chat", group, name, handler, id -> new ChatID(group, id));
    }

    /**
     * Create a new task list with a given name.
     *
     * @param group   the parent group of the task list
     * @param name    the name of the task list
     * @param handler the handler for the response of the server
     */
    public static void createTaskList(GroupID group, String name,
                                      ResultHandler<TaskListID> handler) {
        createModule("task", group, name, handler, id -> new TaskListID(group, id));
    }

    /**
     * Create a new calendar with a given name.
     *
     * @param group   the parent group of the calendar
     * @param name    the name of the calendar
     * @param handler the handler for the response of the server
     */
    public static void createCalendar(GroupID group, String name,
                                      ResultHandler<CalendarID> handler) {
        createModule("cal", group, name, handler, id -> new CalendarID(group, id));
    }

    /**
     * Create a new poll list with a given name.
     *
     * @param group   the parent group of the poll list
     * @param name    the name of the poll list
     * @param handler the handler for the response of the server
     */
    public static void createPollList(GroupID group, String name,
                                      ResultHandler<PollListID> handler) {
        createModule("poll", group, name, handler, id -> new PollListID(group, id));
    }

    /**
     * Create a new custom module with a given name and type.
     *
     * @param group   the parent group of the task list
     * @param name    the name of the task list
     * @param type    the type string of the custom module
     * @param handler the handler for the response of the server
     */
    public static void createCustomModule(GroupID group, String name, String type,
                                          ResultHandler<CustomModuleID> handler) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("type string cannot be empty");
        } else if (ModuleID.isKnownType(type)) {
            throw new IllegalArgumentException("cannot create custom module with type " + type);
        }

        createModule(type, group, name, handler, id -> new CustomModuleID(group, id, type));
    }

    /**
     * Set whether approval is required for user's calendar events in a group.
     *
     * @param group   the group to update the settings for
     * @param requireApproval whether approval is required
     * @param handler the handler for the response of the server
     */
    public static void setRequireApproval(GroupID group, boolean requireApproval,
                                          ResultHandler<Void> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("requireApproval", requireApproval);
        getConnection().send(new ServerMsg("setRequireApproval", data), handler,
                response -> null);
    }

    /**
     * Set whether a module is enabled or disabled.
     *
     * @param module  the module to enable/disable
     * @param enabled whether the module should be enabled
     * @param handler the handler for the response of the server
     */
    public static void setModuleEnabled(ModuleID module, boolean enabled,
                                        ResultHandler<Void> handler) {
        if (module == null) {
            throw new IllegalArgumentException("ModuleID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", module.group.id);
        data.addProperty("id", module.id);
        data.addProperty("enabled", enabled);
        getConnection().send(new ServerMsg("setModuleEnabled", data), handler,
                response -> null);
    }

    /**
     * Get a list of all users in a group, including the current user.
     *
     * @param group   the group to list the users of
     * @param handler the handler for the response of the server
     * @see GroupUserEntry
     */
    public static void getUsers(GroupID group, ResultHandler<GroupUserEntry[]> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        requestArray(new ServerMsg("getUsers", data), handler, GroupUserEntry.class,
                "users", GroupUserEntry::fromJson);
    }

    /**
     * Get a list of all modules in a group. Note that this method directly returns the module info
     * instead of requiring separate calls like other methods, since the server is able to
     * efficiently retrieve this information all at once. However, getModuleInfo() is still provided
     * as a convenience.
     *
     * @param group   the group to list the modules of
     * @param handler the handler for the response of the server
     * @see ModuleInfo
     */
    public static void getModules(GroupID group, ResultHandler<ModuleInfo[]> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        requestArray(new ServerMsg("getModules", data), handler, ModuleInfo.class,
                "modules", json -> ModuleInfo.fromJson(group, json));
    }

    /**
     * Create an invite link to join a group with a specific expiration timestamp. If the timestamp
     * is 0, the link will never expire. Returns a URL which can be used to join the group.
     *
     * @param group           the group to create a link to
     * @param expireTimestamp the timestamp for when the link will expire, or null
     * @param handler         the handler for the response of the server
     */
    public static void createInviteLink(GroupID group, long expireTimestamp,
                                        ResultHandler<String> handler) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (expireTimestamp < 0) {
            throw new IllegalArgumentException("expire timestamp cannot be negative");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("expire", expireTimestamp);
        getConnection().send(new ServerMsg("createInviteLink", data), handler, response ->
                response.get("link").getAsString());
    }

    /**
     * Check if an invite link is valid, and retrieve the data associated with it. If the link is
     * invalid, null will be returned instead of an InviteLinkEntry. Expired links' data are still
     * returned, so use InviteLinkEntry.hasExpired() to check if a link is expired.
     *
     * @param inviteLink the URL of an invite link
     * @param handler    the handler for the response of the server
     * @see InviteLinkEntry
     */
    public static void checkInviteLink(String inviteLink, ResultHandler<InviteLinkEntry> handler) {
        if (inviteLink == null) {
            throw new IllegalArgumentException("invite link cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("link", inviteLink);
        getConnection().send(new ServerMsg("checkInviteLink", data), handler, response -> {
            if (response.get("valid").getAsBoolean()) {
                return InviteLinkEntry.fromJson(inviteLink, response);
            } else {
                return null;
            }
        });
    }

    /**
     * Use an invite link, joining the associated group. If the link is invalid or expired, null
     * will be returned instead of a GroupID, indicating failure to join the group.
     *
     * @param inviteLink the URL of an invite link
     * @param handler    the handler for the response of the server
     */
    public static void useInviteLink(String inviteLink, ResultHandler<GroupID> handler) {
        if (inviteLink == null) {
            throw new IllegalArgumentException("invite link cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("link", inviteLink);
        getConnection().send(new ServerMsg("useInviteLink", data), handler, response -> {
            JsonElement id = response.get("id");
            if (id.isJsonNull()) {
                return null;
            } else {
                return new GroupID(id.getAsString());
            }
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
        getConnection().send(new ServerMsg("sendInvite", data), handler, response ->
                response.get("sent").getAsBoolean());
    }

    /**
     * Get a list of groups that the user has been invited to join.
     *
     * @param handler the handler for the response of the server
     * @see GroupInviteEntry
     */
    public static void getInvites(ResultHandler<GroupInviteEntry[]> handler) {
        requestArray(new ServerMsg("getInvites"), handler, GroupInviteEntry.class,
                "invites", GroupInviteEntry::fromJson);
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
        getConnection().send(new ServerMsg("replyToInvite", data), handler, response -> null);
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
        getConnection().send(new ServerMsg("leaveGroup", data), handler, response -> null);
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
        getConnection().send(new ServerMsg("kick", data), handler, response -> null);
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
        getConnection().send(new ServerMsg("setRole", data), handler, response -> null);
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
        getConnection().send(new ServerMsg("setMuted", data), handler, response -> null);
    }

    /**
     * Upload a file to the server. Returns a string which can be added to a message to display a
     * link to download the file from the server.
     *
     * @param name     the name of the file to upload (not including directory path)
     * @param contents the contents of the file to upload
     * @param handler  the handler for the response of the server
     */
    public static void uploadFile(String name, byte[] contents, ResultHandler<String> handler) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("file name cannot be null or empty");
        } else if (contents == null) {
            throw new IllegalArgumentException("file contents cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        data.addProperty("contents", Base64.encodeToString(contents, Base64.NO_WRAP));
        getConnection().send(new ServerMsg("uploadFile", data), handler,
                response -> "@<https://" + response.get("link").getAsString() + ">");
    }

    /**
     * Send a message in a chat.
     *
     * @param chat    the chat to send the message in
     * @param message the message to send
     * @param handler the handler for the response of the server
     */
    public static void sendMessage(ChatID chat, String message,
                                   ResultHandler<MessageEntry> handler) {
        if (chat == null) {
            throw new IllegalArgumentException("ChatID cannot be null");
        } else if (message == null) {
            throw new IllegalArgumentException("chat message cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", chat.group.id);
        data.addProperty("chat", chat.id);
        data.addProperty("contents", message);
        getConnection().send(new ServerMsg("sendMessage", data), handler,
                response -> MessageEntry.fromJson(chat, response));
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
        } else if (after != null && after.module != chat) {
            throw new IllegalArgumentException("'after' message must be in the same chat");
        } else if (before != null && before.module != chat) {
            throw new IllegalArgumentException("'before' message must be in the same chat");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", chat.group.id);
        data.addProperty("chat", chat.id);
        data.addProperty("after", after == null ? 0 : after.id);
        data.addProperty("before", before == null ? 0 : before.id);
        requestArray(new ServerMsg("getMessages", data), handler, MessageEntry.class,
                "messages", json -> MessageEntry.fromJson(chat, json));
    }

    /**
     * Update a message in a chat. If the newContents is null, the message will be deleted.
     * Otherwise, the contents of the message will be updated to match newContents.
     *
     * @param message     the message to update
     * @param newContents the new contents of the message or null
     * @param handler     the handler for the response of the server
     */
    public static void updateMessage(MessageID message, String newContents,
                                     ResultHandler<MessageEntry> handler) {
        if (message == null) {
            throw new IllegalArgumentException("MessageID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", message.module.group.id);
        data.addProperty("chat", message.module.id);
        data.addProperty("id", message.id);
        data.addProperty("newContents", newContents);
        getConnection().send(new ServerMsg("updateMessage", data), handler,
                response -> MessageEntry.fromJson(message.module, response));
    }

    /**
     * Set the reaction for a message. Any existing reaction will be replaced, so a reaction of
     * NONE can be used to delete any existing reaction. A reaction of UNKNOWN will always fail
     * immediately, since it is not an actual reaction. The new map of reactions will be returned.
     *
     * @param message  the message to set the reaction of
     * @param reaction the reaction to set
     * @param handler  the handler for the response of the server
     */
    public static void setReaction(MessageID message, Reaction reaction,
                                   ResultHandler<Map<UserID, String>> handler) {
        if (message == null) {
            throw new IllegalArgumentException("MessageID cannot be null");
        } else if (reaction == null) {
            throw new IllegalArgumentException("Reaction cannot be null");
        }

        JsonElement reactionJson;
        switch (reaction) {
            case UNKNOWN:
                handler.handleResult(ServerResult.failure("cannot add unknown reaction"));
                return;
            case NONE:
                reactionJson = JsonNull.INSTANCE;
                break;
            default:
                reactionJson = new JsonPrimitive(reaction.name().toLowerCase());
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", message.module.group.id);
        data.addProperty("chat", message.module.id);
        data.addProperty("id", message.id);
        data.add("reaction", reactionJson);
        getConnection().send(new ServerMsg("setReaction", data), handler,
                MessageEntry::parseReactions);
    }

    /**
     * Add a task with a given description to a task list.
     *
     * @param taskList    the task list to add the task to
     * @param deadline    the deadline to set for the task (or 0 for no deadline)
     * @param description the description of the task
     * @param handler     the handler for the response of the server
     */
    public static void addTask(TaskListID taskList, long deadline, String description,
                               ResultHandler<TaskEntry> handler) {
        if (taskList == null) {
            throw new IllegalArgumentException("TaskListID cannot be null");
        } else if (deadline < 0) {
            throw new IllegalArgumentException("task deadline cannot be negative");
        } else if (description == null) {
            throw new IllegalArgumentException("task description cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", taskList.group.id);
        data.addProperty("taskList", taskList.id);
        data.addProperty("deadline", deadline);
        data.addProperty("description", description);
        getConnection().send(new ServerMsg("addTask", data), handler,
                response -> TaskEntry.fromJson(taskList, response));
    }

    /**
     * Get a list of the tasks in a task list.
     *
     * @param taskList the task list to request tasks from
     * @param handler  the handler for the response of the server
     * @see TaskEntry
     */
    public static void getTasks(TaskListID taskList, ResultHandler<TaskEntry[]> handler) {
        if (taskList == null) {
            throw new IllegalArgumentException("TaskListID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", taskList.group.id);
        data.addProperty("taskList", taskList.id);
        requestArray(new ServerMsg("getTasks", data), handler, TaskEntry.class,
                "tasks", json -> TaskEntry.fromJson(taskList, json));
    }

    /**
     * Update a task's status.
     *
     * @param task    the task to update
     * @param status  the status to set for the task
     * @param handler the handler for the response of the server
     * @see TaskStatus
     */
    public static void updateTaskStatus(TaskID task, TaskStatus status,
                                        ResultHandler<TaskEntry> handler) {
        if (task == null) {
            throw new IllegalArgumentException("TaskID cannot be null");
        } else if (status == null) {
            throw new IllegalArgumentException("TaskStatus cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", task.module.group.id);
        data.addProperty("taskList", task.module.id);
        data.addProperty("id", task.id);
        data.addProperty("completed", status == TaskStatus.COMPLETED);
        data.addProperty("inProgress", status == TaskStatus.IN_PROGRESS);
        getConnection().send(new ServerMsg("updateTaskStatus", data), handler,
                response -> TaskEntry.fromJson(task.module, response));
    }

    /**
     * Update a task's deadline.
     *
     * @param task     the task to update
     * @param deadline the deadline to set for the task (or 0 for no deadline)
     * @param handler  the handler for the response of the server
     * @see TaskStatus
     */
    public static void updateTaskDeadline(TaskID task, long deadline,
                                          ResultHandler<TaskEntry> handler) {
        if (task == null) {
            throw new IllegalArgumentException("TaskID cannot be null");
        } else if (deadline < 0) {
            throw new IllegalArgumentException("task deadline cannot be negative");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", task.module.group.id);
        data.addProperty("taskList", task.module.id);
        data.addProperty("id", task.id);
        data.addProperty("deadline", deadline);
        getConnection().send(new ServerMsg("updateTaskDeadline", data), handler,
                response -> TaskEntry.fromJson(task.module, response));
    }

    /**
     * Delete a task from a task list. The TaskID becomes invalid and can no longer be used.
     *
     * @param task    the task to delete
     * @param handler the handler for the response of the server
     */
    public static void deleteTask(TaskID task, ResultHandler<Void> handler) {
        if (task == null) {
            throw new IllegalArgumentException("TaskID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", task.module.group.id);
        data.addProperty("taskList", task.module.id);
        data.addProperty("id", task.id);
        getConnection().send(new ServerMsg("deleteTask", data), handler, response -> null);
    }

    /**
     * Add an event to a calendar. If the user is a moderator, the event will be added directly.
     * Otherwise, a moderator will have to approve the event before it will show up in the calendar.
     *
     * @param calendar    the calendar to add the event to
     * @param description the description of the event
     * @param start       the start timestamp of the event
     * @param end         the end timestamp of the event
     * @param handler     the handler for the response of the server
     */
    public static void addEvent(CalendarID calendar, String description, long start, long end,
                                ResultHandler<EventEntry> handler) {
        if (calendar == null) {
            throw new IllegalArgumentException("CalendarID cannot be null");
        } else if (description == null) {
            throw new IllegalArgumentException("event description cannot be null");
        } else if (start < 1) {
            throw new IllegalArgumentException("event start timestamp cannot be less than 1");
        } else if (end < start) {
            throw new IllegalArgumentException("event end cannot come before start");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", calendar.group.id);
        data.addProperty("calendar", calendar.id);
        data.addProperty("description", description);
        data.addProperty("start", start);
        data.addProperty("end", end);
        getConnection().send(new ServerMsg("addEvent", data), handler,
                response -> EventEntry.fromJson(calendar, response));
    }

    /**
     * Get a list of the events in a calendar.
     *
     * @param calendar the calendar to request events from
     * @param handler  the handler for the response of the server
     */
    public static void getEvents(CalendarID calendar, ResultHandler<EventEntry[]> handler) {
        if (calendar == null) {
            throw new IllegalArgumentException("CalendarID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", calendar.group.id);
        data.addProperty("calendar", calendar.id);
        requestArray(new ServerMsg("getEvents", data), handler, EventEntry.class,
                "events", json -> EventEntry.fromJson(calendar, json));
    }

    /**
     * As a moderator, approve or deny an event created by a user. If the event is denied, the
     * event will be deleted so the EventID becomes invalid and can no longer be used.
     *
     * @param event   the event to approve
     * @param approve whether to approve the event
     * @param handler the handler for the response of the server
     */
    public static void approveEvent(EventID event, boolean approve, ResultHandler<Void> handler) {
        if (event == null) {
            throw new IllegalArgumentException("EventID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", event.module.group.id);
        data.addProperty("calendar", event.module.id);
        data.addProperty("id", event.id);
        data.addProperty("approve", approve);
        getConnection().send(new ServerMsg("approveEvent", data), handler, response -> null);
    }

    /**
     * Delete an event from a calendar. The EventID becomes invalid and can no longer be used.
     *
     * @param event   the event to delete
     * @param handler the handler for the response of the server
     */
    public static void deleteEvent(EventID event, ResultHandler<Void> handler) {
        if (event == null) {
            throw new IllegalArgumentException("EventID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", event.module.group.id);
        data.addProperty("calendar", event.module.id);
        data.addProperty("id", event.id);
        getConnection().send(new ServerMsg("deleteEvent", data), handler, response -> null);
    }

    /**
     * As a moderator, set whether an event should appear on a bulletin board.
     *
     * @param event    the event
     * @param bulletin whether the event should appear on a bulletin board
     * @param handler  the handler for the response of the server
     */
    public static void setBulletin(EventID event, boolean bulletin, ResultHandler<Void> handler) {
        if (event == null) {
            throw new IllegalArgumentException("EventID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", event.module.group.id);
        data.addProperty("calendar", event.module.id);
        data.addProperty("id", event.id);
        data.addProperty("bulletin", bulletin);
        getConnection().send(new ServerMsg("setBulletin", data), handler, response -> null);
    }

    /**
     * Add a poll with a given description and options to a poll list.
     *
     * @param pollList    the poll list to add the poll to
     * @param description the description of the poll
     * @param options     the descriptions of the poll's options
     * @param handler     the handler for the response of the server
     */
    public static void addPoll(PollListID pollList, String description, List<String> options,
                               ResultHandler<PollEntry> handler) {
        if (pollList == null) {
            throw new IllegalArgumentException("PollListID cannot be null");
        } else if (description == null) {
            throw new IllegalArgumentException("poll description cannot be null");
        } else if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("poll options cannot be null or empty");
        }

        JsonArray array = new JsonArray();
        for (String option : options) {
            array.add(option);
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", pollList.group.id);
        data.addProperty("pollList", pollList.id);
        data.addProperty("description", description);
        data.add("options", array);
        getConnection().send(new ServerMsg("addPoll", data), handler,
                response -> PollEntry.fromJson(pollList, response));
    }

    /**
     * Get a list of the polls in a poll list.
     *
     * @param pollList the poll list to request polls from
     * @param handler  the handler for the response of the server
     * @see PollEntry
     */
    public static void getPolls(PollListID pollList, ResultHandler<PollEntry[]> handler) {
        if (pollList == null) {
            throw new IllegalArgumentException("PollListID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", pollList.group.id);
        data.addProperty("pollList", pollList.id);
        requestArray(new ServerMsg("getPolls", data), handler, PollEntry.class,
                "polls", json -> PollEntry.fromJson(pollList, json));
    }

    /**
     * Vote for a specific option on a poll.
     *
     * @param poll    the poll to vote on
     * @param option  the option to vote for
     * @param handler the handler for the response of the server
     */
    public static void voteOnPoll(PollID poll, int option, ResultHandler<Void> handler) {
        if (poll == null) {
            throw new IllegalArgumentException("PollID cannot be null");
        } else if (option < 0) {
            throw new IllegalArgumentException("poll vote option cannot be negative");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", poll.module.group.id);
        data.addProperty("pollList", poll.module.id);
        data.addProperty("id", poll.id);
        data.addProperty("option", option);
        getConnection().send(new ServerMsg("voteOnPoll", data), handler, response -> null);
    }

    /**
     * Get the info of multiple GroupIDs. Only the info of groups which have been updated more
     * recently than lastRefresh will be returned, otherwise they will be omitted. If lastRefresh
     * is 0, the group info will always be retrieved.
     *
     * @param groups      the groups to retrieve the info of
     * @param lastRefresh the last time the cached info was refreshed or 0
     * @param handler     the handler for the response of the server
     * @see GroupInfo
     */
    public static void getGroupInfo(Collection<GroupID> groups, long lastRefresh,
                                    ResultHandler<GroupInfo[]> handler) {
        getInfo(GroupInfo.class, "groups", "getGroupInfo", GroupInfo::fromJson,
                groups, lastRefresh, handler);
    }

    /**
     * Get the info of a UserID. The info will only be retrieved if it has been updated more
     * recently than lastRefresh, otherwise null will be returned. If lastRefresh is 0, the user
     * info will always be retrieved.
     *
     * @param user        the user to retrieve the info of
     * @param lastRefresh the last time the cached info was refreshed or 0
     * @param handler     the handler for the response of the server
     * @see UserInfo
     */
    public static void getUserInfo(UserID user, long lastRefresh,
                                   ResultHandler<UserInfo> handler) {
        getUserInfo(Collections.singleton(user), lastRefresh, result ->
            handler.handleResult(result.map(users -> {
                switch (users.length) {
                    case 0:
                        return null;
                    case 1:
                        return users[0];
                    default:
                        throw new IllegalArgumentException("multiple users returned");
                }
            })));
    }

    /**
     * Get the info of multiple UserIDs. Only the info of users which have been updated more
     * recently than lastRefresh will be returned, otherwise they will be omitted. If lastRefresh
     * is 0, the user info will always be retrieved.
     *
     * @param users       the users to retrieve the info of
     * @param lastRefresh the last time the cached info was refreshed or 0
     * @param handler     the handler for the response of the server
     * @see UserInfo
     */
    public static void getUserInfo(Collection<UserID> users, long lastRefresh,
                                   ResultHandler<UserInfo[]> handler) {
        getInfo(UserInfo.class, "users", "getUserInfo", UserInfo::fromJson,
                users, lastRefresh, handler);
    }

    /**
     * Get the info of multiple ModuleIDs.
     *
     * @param modules the modules to retrieve the info of
     * @param handler the handler for the response of the server
     * @see ModuleInfo
     */
    public static void getModuleInfo(Collection<ModuleID> modules,
                                     ResultHandler<ModuleInfo[]> handler) {
        getInfo(ModuleInfo.class, "modules", "getModuleInfo",
                json -> ModuleInfo.fromJson(null, json), modules, -1, handler);
    }

    /**
     * Generic helper method for creating different types of modules. Used to implement
     * createChat(), createTaskList(), and createCustomModule().
     *
     * @param type    the type string of the module
     * @param group   the group to create the module in
     * @param name    the name of the module
     * @param handler the handler for the response of the server
     * @param module  a function to convert a module ID string into a specific type of ModuleID
     * @param <T>     the type of the module ID
     */
    private static <T> void createModule(String type, GroupID group, String name,
                                         ResultHandler<T> handler, Function<String, T> module) {
        if (group == null) {
            throw new IllegalArgumentException("GroupID cannot be null");
        } else if (name == null) {
            throw new IllegalArgumentException("module name cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", group.id);
        data.addProperty("name", name);
        data.addProperty("type", type);
        getConnection().send(new ServerMsg("createModule", data), handler, response ->
                module.apply(response.get("id").getAsString()));
    }

    /**
     * Generic helper method for retrieving info about an ItemID from the server. Used to implement
     * getGroupInfo(), getUserInfo(), and getModuleInfo().
     *
     * @param clazz       the class of the info
     * @param field       the name of the field in the request
     * @param request     the kind of the request
     * @param parser      a parser for the server response
     * @param ids         the ids to retrieve the info of
     * @param lastRefresh the last time the cached info was refreshed or 0 (or omitted if negative)
     * @param handler     the handler for the response of the server
     * @param <T>         the type of the info
     * @param <U>         the type of the item
     */
    @SuppressWarnings("unchecked")
    private static <T, U extends ItemID> void getInfo(Class<T> clazz, String field, String request,
                                                      Function<JsonObject, T> parser,
                                                      Collection<U> ids, long lastRefresh,
                                                      ResultHandler<T[]> handler) {
        if (ids == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }

        if (ids.isEmpty()) {
            then(() -> handler.handleResult(
                    ServerResult.success((T[]) Array.newInstance(clazz, 0))));
            return;
        }

        JsonArray array = new JsonArray();
        for (ItemID id : ids) {
            if (id == null) {
                throw new IllegalArgumentException("ItemID cannot be null");
            }
            array.add(id.toJson());
        }

        JsonObject data = new JsonObject();
        data.add(field, array);
        if (lastRefresh >= 0) {
            data.addProperty("lastRefresh", lastRefresh);
        }
        requestArray(new ServerMsg(request, data), handler, clazz, field, parser);
    }

    /**
     * Generic helper method for handling requests that return arrays.
     *
     * @param message the message to send to the server
     * @param handler the handler for the response of the server
     * @param clazz   the class of the item in the array
     * @param field   the name of the array in the response
     * @param parser  a parser for the server response
     * @param <T>     the type of the item in the array
     */
    @SuppressWarnings("unchecked")
    private static <T> void requestArray(ServerMsg message, ResultHandler<T[]> handler,
                                         Class<T> clazz, String field,
                                         Function<JsonObject, T> parser) {
        getConnection().send(message, handler, response -> {
            JsonArray json = response.getAsJsonArray(field);
            T[] array = (T[]) Array.newInstance(clazz, json.size());
            for (int i = 0; i < array.length; i++) {
                array[i] = parser.apply(json.get(i).getAsJsonObject());
            }
            return array;
        });
    }
}
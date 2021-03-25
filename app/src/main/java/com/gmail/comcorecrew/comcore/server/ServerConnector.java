package com.gmail.comcorecrew.comcore.server;

import android.os.Handler;
import android.os.Looper;

import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.notifications.NotificationListener;
import com.gmail.comcorecrew.comcore.server.connection.Connection;
import com.gmail.comcorecrew.comcore.server.connection.Function;
import com.gmail.comcorecrew.comcore.server.connection.ServerMsg;
import com.gmail.comcorecrew.comcore.server.entry.*;
import com.gmail.comcorecrew.comcore.server.id.*;
import com.gmail.comcorecrew.comcore.server.info.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
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
                ArrayDeque<NotificationListener> listeners = new ArrayDeque<>(notificationListeners);
                while (!listeners.isEmpty()) {
                    try {
                        NotificationListener listener = listeners.pop();
                        if (function.apply(listener)) {
                            break;
                        }
                        Collection<? extends NotificationListener> children = listener.getChildren();
                        if (children != null) {
                            listeners.addAll(children);
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
    public static UserInfo getUser() {
        return getConnection().getUserInfo();
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
     * @param name the name of the group
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
     * Send a message in a chat.
     *
     * @param chat    the chat to send the message in
     * @param message the message to send
     * @param handler the handler for the response of the server
     */
    public static void sendMessage(ChatID chat, String message, ResultHandler<MessageID> handler) {
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
                response -> new MessageID(chat, response.get("id").getAsLong()));
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
                                     ResultHandler<Void> handler) {
        if (message == null) {
            throw new IllegalArgumentException("MessageID cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", message.module.group.id);
        data.addProperty("chat", message.module.id);
        data.addProperty("id", message.id);
        data.addProperty("newContents", newContents);
        getConnection().send(new ServerMsg("updateMessage", data), handler, response -> null);
    }

    /**
     * Add a task with a given description to a task list.
     *
     * @param taskList    the task list to add the task to
     * @param description the description of the task
     * @param handler     the handler for the response of the server
     */
    public static void addTask(TaskListID taskList, String description,
                               ResultHandler<TaskID> handler) {
        if (taskList == null) {
            throw new IllegalArgumentException("TaskListID cannot be null");
        } else if (description == null) {
            throw new IllegalArgumentException("task description cannot be null");
        }

        JsonObject data = new JsonObject();
        data.addProperty("group", taskList.group.id);
        data.addProperty("taskList", taskList.id);
        data.addProperty("description", description);
        getConnection().send(new ServerMsg("addTask", data), handler,
                response -> new TaskID(taskList, response.get("id").getAsLong()));
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
     * Update a task's completed status.
     *
     * @param task      the task to update
     * @param completed whether the task has been completed
     * @param handler   the handler for the response of the server
     */
    public static void updateTask(TaskID task, boolean completed, ResultHandler<Void> handler) {
        if (task == null) {
            throw new IllegalArgumentException("TaskID cannot be null");
        }

        // TODO implement
        handler.handleResult(ServerResult.failure("unimplemented: updateTask"));
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

        // TODO implement
        handler.handleResult(ServerResult.failure("unimplemented: deleteTask"));
    }

    /**
     * Get the info of a GroupID. The info will only be retrieved if it has been updated more
     * recently than lastRefresh, otherwise null will be returned. If lastRefresh is 0, the group
     * info will always be retrieved.
     *
     * @param group       the group to retrieve the info of
     * @param lastRefresh the last time the cached info was refreshed or 0
     * @param handler     the handler for the response of the server
     * @see GroupInfo
     */
    public static void getGroupInfo(GroupID group, long lastRefresh,
                                    ResultHandler<GroupInfo> handler) {
        getGroupInfo(Collections.singletonList(group), lastRefresh, result ->
            handler.handleResult(result.map(groups -> {
                switch (groups.length) {
                    case 0:
                        return null;
                    case 1:
                        return groups[0];
                    default:
                        throw new IllegalArgumentException("multiple groups returned");
                }
            })));
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
    public static void getGroupInfo(List<GroupID> groups, long lastRefresh,
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
        getUserInfo(Collections.singletonList(user), lastRefresh, result ->
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
    public static void getUserInfo(List<UserID> users, long lastRefresh,
                                   ResultHandler<UserInfo[]> handler) {
        getInfo(UserInfo.class, "users", "getUserInfo", UserInfo::fromJson,
                users, lastRefresh, handler);
    }

    /**
     * Get the info of a ModuleID.
     *
     * @param module  the module to retrieve the info of
     * @param handler the handler for the response of the server
     * @see ModuleInfo
     */
    public static void getModuleInfo(ModuleID module, ResultHandler<ModuleInfo> handler) {
        getModuleInfo(Collections.singletonList(module), result ->
            handler.handleResult(result.map(modules -> {
                switch (modules.length) {
                    case 0:
                        return null;
                    case 1:
                        return modules[0];
                    default:
                        throw new IllegalArgumentException("multiple modules returned");
                }
            })));
    }

    /**
     * Get the info of multiple ModuleIDs.
     *
     * @param modules the modules to retrieve the info of
     * @param handler the handler for the response of the server
     * @see ModuleInfo
     */
    public static void getModuleInfo(List<ModuleID> modules, ResultHandler<ModuleInfo[]> handler) {
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
                                                      List<U> ids, long lastRefresh,
                                                      ResultHandler<T[]> handler) {
        if (ids == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }

        if (ids.isEmpty()) {
            handler.handleResult(ServerResult.success((T[]) Array.newInstance(clazz, 0)));
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
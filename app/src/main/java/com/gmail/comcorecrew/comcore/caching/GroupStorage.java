package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.exceptions.InvalidFileFormatException;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.gmail.comcorecrew.comcore.server.info.GroupInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class for storing group and module data into device files.
 *
 * NOTE: AppData.init() must be run before this module works!
 */
public class GroupStorage {
    private GroupStorage() {}

    /**
     * Refresh the list of groups, calling the provided Runnable on completion.
     *
     * @param callback the callback to run (or null)
     */
    public static void refresh(Runnable callback) {
        // Get the group associated with each ID
        HashMap<GroupID, Group> existingIds = new HashMap<>(AppData.groups.size());
        for (int i = 0; i < AppData.groups.size(); i++) {
            Group group = AppData.groups.get(i);
            GroupID id = group.getGroupId();

            // Remove any duplicated groups
            if (existingIds.containsKey(id)) {
                AppData.groups.remove(i--);
                continue;
            }

            existingIds.put(id, group);
        }

        // Get all of the groups that the user is part of
        ServerConnector.getGroups(result -> {
            HashMap<GroupID, Group> ids;
            if (result.isSuccess()) {
                // Update the list of groups if the request was successful
                ids = new HashMap<>(existingIds.size());
                AppData.groups.clear();
                for (GroupID id : result.data) {
                    // Check for an existing group object to reuse
                    Group group = existingIds.get(id);
                    if (group != null) {
                        ids.put(id, group);
                        AppData.groups.add(group);
                        continue;
                    }

                    // Create a new group object for this new group since it wasn't present before
                    group = new Group(id);
                    AppData.groups.add(group);
                    ids.put(id, group);
                }
            } else {
                ids = existingIds;
            }

            // Update the group info of any existing groups
            ServerConnector.getGroupInfo(ids.keySet(), 0, resultGroups -> {
                if (resultGroups.isFailure()) {
                    if (callback != null) {
                        callback.run();
                    }
                    return;
                }

                // Iterate over every group and refresh their user information as well
                HashSet<UserID> alreadyRefreshed = new HashSet<>();
                for (GroupInfo info : resultGroups.data) {
                    Group group = ids.get(info.id);
                    if (group == null) {
                        continue;
                    }

                    // Update the group's info
                    group.setName(info.name);
                    group.setGroupRole(info.role);
                    group.setMuted(info.muted);

                    // Refresh the group's users (and store the group data)
                    group.refreshUsers(alreadyRefreshed, null);

                    // Refresh the group's modules (and store the module data)
                    group.refreshModules(null);
                }

                // Run the callback after setting all of the info
                if (callback != null) {
                    callback.run();
                }
            });
        });
    }

    /**
     * Stores all groups in AppData.groups to storage files.
     *
     * @throws IOException if an IO error occurs
     */
    public static void storeAllGroups() throws IOException {
        for (Group group : AppData.groups) {
            storeGroup(group);
        }
    }

    /**
     * Stores a specific group and its modules to storage files.
     *
     * @param group group to store
     * @throws IOException if an IO error occurs
     */
    public static void storeGroup(Group group) throws IOException {
        File groupDir = new File(AppData.groupsDir, group.getGroupId().id);
        if ((!groupDir.exists()) && (!groupDir.mkdir())) {
            throw new IOException("Cannot store group: " + group.getGroupId().id);
        }

        storeGroupData(group);
        storeGroupModules(group);
    }

    /**
     * Stores only the group data of a specific group to storage files.
     *
     * @param group group to store info
     * @throws IOException if an IO error occurs
     */
    public static void storeGroupData(Group group) throws IOException {
        File groupData = new File(AppData.groupsDir, group.getGroupId().id + "/grData");
        if ((!groupData.exists()) && (!groupData.createNewFile())) {
            throw new IOException("Cannot store group data: " + group.getGroupId().id);
        }

        PrintWriter writer = new PrintWriter(groupData);

        AppData.writeString(group.getName(), writer);
        AppData.writeString(group.getGroupRole().toString(), writer);
        AppData.writeBool(group.getMuted(), writer);
        AppData.writeBool(group.isPinned(), writer);

        AppData.writeInt(group.getUsers().size(), writer);
        for (User user : group.getUsers()) {
            AppData.writeInt(UserStorage.getInternalId(user.getID()), writer);
        }
        AppData.writeInt(group.getModerators().size(), writer);
        for (UserID id : group.getModerators()) {
            AppData.writeInt(UserStorage.getInternalId(id), writer);
        }
        AppData.writeInt(UserStorage.getInternalId(group.getOwner()), writer);

        writer.close();
    }

    /**
     * Stores only the modules of a group to storage files.
     *
     * @param group group to store modules of
     * @throws IOException if an IO error occurs
     */
    public static void storeGroupModules(Group group) throws IOException {
        for (Module module : group.getModules()) {
            storeModule(module);
        }
    }

    /**
     * Stores a specific module to storage files.
     *
     * @param module module to store
     * @throws IOException if an IO error occurs
     */
    public static void storeModule(Module module) throws IOException {
        File moduleData = new File(AppData.groupsDir, module.getLocatorString());
        if ((!moduleData.exists()) && (!moduleData.createNewFile())) {
            throw new IOException("Cannot store module: " + module.getLocatorString());
        }

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(moduleData));
        out.writeObject(module);
        out.close();
    }

    /**
     * Reads all of the groups from storage files into AppData.groups
     *
     * @throws IOException if an IO error occurs
     */
    public static void readAllGroups() throws IOException {
        AppData.groups = new ArrayList<>();
        File[] files = AppData.groupsDir.listFiles();
        if (files == null) {
            throw new InvalidFileFormatException("Cannot read groups directory!");
        }
        for (File file : files) {
            AppData.groups.add(new Group(new GroupID(file.getName())));
            readGroup(AppData.groups.get(AppData.groups.size() - 1));
            AppData.groups.get(AppData.groups.size() - 1).setIndex(AppData.groups.size() - 1);
        }
    }

    /**
     * Reads a specific groups data from storage files.
     *
     * @param group group to read data of
     * @throws IOException if an IO error occurs
     */
    public static void readGroup(Group group) throws IOException {
        File groupData = new File(AppData.groupsDir, group.getGroupId().id + "/grData");
        if (!groupData.exists()) {
            throw new InvalidFileFormatException("Group data missing for " + group.getGroupId().id);
        }
        BufferedReader reader = new BufferedReader(new FileReader(groupData));

        group.setName(AppData.readString(reader));
        group.setGroupRole(GroupRole.fromString(AppData.readString(reader)));
        group.setMuted(AppData.readBool(reader));
        group.setPinned(AppData.readBool(reader));

        int length = AppData.readInt(reader);
        ArrayList<User> users = new ArrayList<>();
        User user;

        for (int i = 0; i < length; i++) {
            user = UserStorage.getUser(AppData.readInt(reader));
            if (user == null) {
                throw new InvalidFileFormatException("Null user stored");
            }
            users.add(user);
        }
        group.setUsers(users);

        length = AppData.readInt(reader);
        ArrayList<UserID> moderators = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            user = UserStorage.getUser(AppData.readInt(reader));
            if (user == null) {
                throw new InvalidFileFormatException("Null user stored");
            }
            moderators.add(user.getID());
        }
        group.setModerators(moderators);
        user = UserStorage.getUser(AppData.readInt(reader));
        if (user == null) {
            throw new InvalidFileFormatException("Null user stored");
        }
        group.setOwner(user.getID());

        reader.close();

        // Reads and adds modules
        ArrayList<Module> modules = new ArrayList<>();
        File[] files = new File(AppData.groupsDir, group.getGroupId().id).listFiles();
        if (files == null) {
            throw new IOException("Cannot open directory: " + group.getGroupId().id);
        }
        try {
            for (File file : files) {
                if (!file.getName().equals("grData")) {

                    ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
                    Object o = stream.readObject();
                    Module m = (Module) o;
                    m.setGroup(group);
                    m.fromCache();
                    m.setIndex(modules.size());
                    modules.add(m);
                    stream.close();

                }
            }
        } catch (ClassNotFoundException e) {
            throw new InvalidFileFormatException("Class not found");
        }

        group.setModules(modules);
    }

    /**
     * Adds a new group to AppData.groups and saves group data to storage files.
     *
     * @param group group to add and save
     * @return true if group is added; false if group already exists in files
     * @throws IOException if an IO error occurs
     */
    public static boolean addGroup(Group group) throws IOException {
        for (Group g : AppData.groups) {
            if (g.getGroupId().id.equals(group.getGroupId().id)) {
                return false;
            }
        }
        AppData.groups.add(group);
        storeGroup(group);
        return true;
    }

    /**
     * Gets the Group object corresponding to a GroupID
     * @param id the ID of the group
     * @return the Group object
     */
    public static Group getGroup(GroupID id) {
        for (Group group : AppData.groups) {
            if (group.getGroupId().equals(id)) {
                return group;
            }
        }

        return null;
    }
}

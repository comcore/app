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

/**
 * Class for storing group and module data into device files.
 *
 * NOTE: AppData.init() must be run before this module works!
 */
public class GroupStorage {
    /**
     * Look up the Group corresponding to a GroupID, or fetch it from the server if it is not
     * already in the GroupStorage. If it succeeds, the callback will be called with the retrieved
     * information about the group.
     *
     * @param id       the ID of the group
     * @param callback what to do with the result
     */
    public static void lookup(GroupID id, LookupCallback<Group> callback) {
        // Try to find the group in the AppData
        for (Group group : AppData.groups) {
            if (group.getGroupId().equals(id)) {
                callback.accept(group);
                return;
            }
        }

        // Otherwise, get the info from the server and cache it
        ServerConnector.getGroupInfo(id, 0, result -> {
            // Make sure it hasn't already been added
            for (Group group : AppData.groups) {
                if (group.getGroupId().equals(id)) {
                    callback.accept(group);
                    return;
                }
            }

            // Make sure the request succeeded
            if (result.isFailure()) {
                return;
            }

            // Add the new group to the AppData
            try {
                Group group = new Group(result.data.name, id, result.data.role, result.data.muted);
                AppData.groups.add(group);
                callback.accept(group);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Stores all groups in AppData.groups to storage files.
     *
     * @throws IOException if an IO error occurs
     */
    public static void StoreAllGroups() throws IOException {
        for (Group group : AppData.groups) {
            StoreGroup(group);
        }
    }

    /**
     * Stores a specific group and its modules to storage files.
     *
     * @param group group to store
     * @throws IOException if an IO error occurs
     */
    public static void StoreGroup(Group group) throws IOException {
        File groupDir = new File(AppData.groupsDir, group.getGroupId().id);
        if ((!groupDir.exists()) && (!groupDir.mkdir())) {
            throw new IOException("Cannot store group: " + group.getGroupId().id);
        }

        StoreGroupData(group);
        StoreGroupModules(group);
    }

    /**
     * Stores only the group data of a specific group to storage files.
     *
     * @param group group to store info
     * @throws IOException if an IO error occurs
     */
    public static void StoreGroupData(Group group) throws IOException {
        File groupData = new File(AppData.groupsDir, group.getGroupId().id + "/grData");
        if ((!groupData.exists()) && (!groupData.createNewFile())) {
            throw new IOException("Cannot store group data: " + group.getGroupId().id);
        }

        PrintWriter writer = new PrintWriter(groupData);

        AppData.writeString(group.getName(), writer);
        AppData.writeString(group.getGroupRole().toString(), writer);
        AppData.writeBool(group.getIsMuted(), writer);

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
    public static void StoreGroupModules(Group group) throws IOException {
        for (Module module : group.getModules()) {
            StoreModule(module);
        }
    }

    /**
     * Stores a specific module to storage files.
     *
     * @param module module to store
     * @throws IOException if an IO error occurs
     */
    public static void StoreModule(Module module) throws IOException {
        File moduleData = new File(AppData.groupsDir, module.getGroupIdString() + "/"
                + module.getLocatorString());
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
    public boolean AddGroup(Group group) throws IOException {
        for (Group g : AppData.groups) {
            if (g.getGroupId().id.equals(group.getGroupId().id)) {
                return false;
            }
        }
        AppData.groups.add(group);
        StoreGroup(group);
        return true;
    }
}

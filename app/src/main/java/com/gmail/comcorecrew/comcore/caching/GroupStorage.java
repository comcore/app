package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.classes.modules.Messaging;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.exceptions.InvalidFileFormatException;
import com.gmail.comcorecrew.comcore.interfaces.Module;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class GroupStorage {

    public static void StoreGroup(Group group) throws IOException {
        File groupDir = new File(AppData.groupsDir, group.getGroupId().id);
        if ((!groupDir.exists()) && (!groupDir.mkdir())) {
            throw new IOException();
        }

        StoreGroupData(group);
        StoreGroupModules(group);
    }

    public static void StoreGroupData(Group group) throws IOException {
        File groupData = new File(AppData.groupsDir, group.getGroupId().id + "/grData");
        if ((!groupData.exists()) && (!groupData.createNewFile())) {
            throw new IOException();
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

    public static void StoreGroupModules(Group group) throws IOException {
        for (Module module : group.getModules()) {
            StoreModule(module);
        }
    }

    public static void StoreModule(Module module) throws IOException {
        File moduleData = new File(AppData.groupsDir, module.getGroupId() + "/"
                + module.getMdid().toString() + module.getMnum());
        if ((!moduleData.exists()) && (!moduleData.createNewFile())) {
            throw new IOException();
        }
        module.toFile();
    }

    public static void readAllGroups() throws IOException {
        AppData.groups = new ArrayList<>();
        File[] files = AppData.groupsDir.listFiles();
        if (files == null) {
            throw new InvalidFileFormatException("Cannot read groups directory");
        }
        for (File file : files) {
            AppData.groups.add(new Group(new GroupID(file.getName())));
            readGroup(AppData.groups.get(AppData.groups.size() - 1));
        }
    }

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
        String filename;
        String mdid;
        File[] files = new File(AppData.groupsDir, group.getGroupId().id).listFiles();
        if (files == null) {
            throw new IOException("Cannot open directory: " + group.getGroupId().id);
        }

        for (File file : files) {

            filename = file.getName();
            if (!filename.equals("grData")) {

                mdid = file.getName().substring(0, 4);

                switch (Mdid.fromString(mdid)) {
                    case CMSG:
                        Messaging mod = new Messaging();
                        mod.fromFile(file, group);
                        modules.add(mod);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid mdid: " + mdid);
                }
            }
        }

        group.setModules(modules);
    }
}

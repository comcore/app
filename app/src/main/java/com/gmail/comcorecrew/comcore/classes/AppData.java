package com.gmail.comcorecrew.comcore.classes;

import android.content.Context;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.modules.DummyButton;
import com.gmail.comcorecrew.comcore.exceptions.StorageFileDisjunctionException;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Singleton class to store data and contains helper functions.
 */
public class AppData {

    public static User self;
    public static File cacheDir;
    public static File filesDir;
    public static File groupsDir;
    public static ArrayList<Group> groups;
    public static final int maxData = 0x001E8483; //4MB + 6 Bytes of chars

    /**
     * Init method that should be run when app is opened.
     *
     * @param context App context
     */
    public static void init(UserInfo user, Context context) throws IOException {
        self = new User(user);
        cacheDir = new File(context.getCacheDir(), self.getID().id);
        filesDir = new File(context.getFilesDir(), self.getID().id);
        boolean madeDir = filesDir.mkdir();
        UserStorage.init();
        if ((madeDir) && (!UserStorage.addUser(self))) {
            throw new StorageFileDisjunctionException("Impossible use storage state.");
        }
        if ((!cacheDir.exists()) && (!cacheDir.mkdir())) {
            throw new IOException("Cannot create cache directory");
        }
        groupsDir = new File(filesDir, "groups");
        if (!groupsDir.mkdir()) {
            GroupStorage.readAllGroups();
        }
        else {
            groups = new ArrayList<>();
        }
        //test
        //new DummyButton("Dummy", getGroup(0));
    }

    /**
     * Fetches the group with the given group id
     *
     * @param groupID   the group id of the requested group.
     * @return          the requested group; null if it does not exist
     */
    public static Group getGroup(GroupID groupID) {
        for (Group group : groups) {
            if (group.getGroupId().id.equals(groupID.id)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Fetches the group with the given index
     *
     * @param index the index of the group
     * @return      the requested group; null if it does not exist
     */
    public static Group getGroup(int index) {
        if ((index < 0) || (index >= groups.size())) {
            return null;
        }
        else {
            return groups.get(index);
        }
    }

    /**
     * Fetches the module at the given indices
     *
     * @param groupIndex    the index of the group
     * @param moduleIndex   the index of the module
     * @return              the requested module; null if it does not exist
     */
    public static Module getModule(int groupIndex, int moduleIndex) {
        if ((groupIndex < 0) || (groupIndex >= groups.size())) {
            return null;
        }
        return groups.get(groupIndex).getModule(moduleIndex);
    }

    /**
     * Fetches the requested module
     *
     * @param address   the address of the module
     * @return          the requested module; null if it does not exist
     */
    public static Module getModule(int[] address) {
        if ((address.length != 2) || (address[0] < 0) || (address[0] >= groups.size())) {
            return null;
        }
        return groups.get(address[0]).getModule(address[1]);
    }

    public static void deleteGroup(Group group) {
        deleteGroup(group.getIndex());
    }

    public static void deleteGroup(GroupID groupID) {
        for (int i = 0; i < groups.size(); i++) {
            if (groupID.equals(groups.get(i).getGroupId())) {
                deleteGroup(i);
                return;
            }
        }
    }

    public static void deleteGroup(int index) {
        if ((index < 0) || (index >= groups.size())) {
            return;
        }
        for (int i = index; i < groups.size() - 1; i++) {
            groups.set(i, groups.get(i + 1));
            groups.get(i).setIndex(i);
        }
        groups.remove(groups.size() - 1);
    }

    public static void normalizeGroupList() {
        Collections.sort(groups);
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).setIndex(i);
        }
    }

    public static void writeBool(boolean b, Writer writer) throws IOException {
        if (b) {
            writer.write('1');
        }
        else {
            writer.write('0');
        }
    }

    public static void writeInt(int i, Writer writer) throws IOException {
        writer.write((char) (i >> 16));
        writer.write((char) i);
    }

    public static void writeLong(long l, Writer writer) throws IOException {
        writer.write((char) (l >> 48));
        writer.write((char) (l >> 32));
        writer.write((char) (l >> 16));
        writer.write((char) l);
    }

    public static void writeString(String string, Writer writer) throws IOException {
        writeInt(string.length(), writer);
        writer.write(string);
    }

    public static boolean readBool(Reader reader) throws IOException {
        switch ((char) reader.read()) {
            case '1':
                return true;
            case '0':
                return false;
            default:
                throw new IOException();
        }
    }

    public static int readInt(Reader reader) throws IOException {
        return (reader.read() << 16) | reader.read();
    }

    public static long readLong(Reader reader) throws IOException {
        long value = reader.read();
        value = (value << 16) | reader.read();
        value = (value << 16) | reader.read();
        return  (value << 16) | reader.read();
    }

    public static String readString(Reader reader) throws IOException {
        int length = readInt(reader);
        char[] buf = new char[length];
        if (reader.read(buf) != length) {
            throw new IOException();
        }
        return String.copyValueOf(buf);
    }
}

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

//NOTE: The position of the group is the group's position in the list of groups. This can change
//      if the group list gets rearranged or a new group is added. The group index is static and
//      does not change as long as the app is open. You can use the index of a group to safely
//      pass groups as only an integer. You should not save references to a group this way, as
//      the group index may change when the app gets opened.

/**
 * Singleton class to store data and contains helper functions.
 */
public class AppData {

    public static User self;
    public static File cacheDir;
    public static File filesDir;
    public static File groupsDir;
    private static Group[] groups;
    private static ArrayList<Group> groupsList;
    private static int groupLength;
    private static int initialGroupLength = 10;
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
        } else {
            clearGroups();
        }
    }

    /**
     * Fetches the list of groups in an ArrayList.
     *
     * @return list of groups
     */
    public static ArrayList<Group> getGroups() {
        return groupsList;
    }

    /**
     * Returns the group at the given position in storage.
     *
     * @param position  the location of the group in storage
     * @return          the group at this given location; null if it does not exist
     */
    public static Group getFromPos(int position) {
        if ((position < 0) || (position > groupsList.size())) {
            return null;
        }

        return groupsList.get(position);
    }

    /**
     * Gets a groups position in the group list
     *
     * @param group group to get position of
     * @return      position of the given group; -1 if does not exist.
     */
    public static int getPosition(Group group) {
        return getPosition(group.getIndex());
    }

    /**
     * Gets the position of the group with the given groupId
     *
     * @param groupID   id of the requested group
     * @return          position of the requested group; -1 if it does not exist
     */
    public static int getPosition(GroupID groupID) {
        for (int i = 0; i < groupsList.size(); i++) {
            if (groupID.id.equals(groupsList.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the position of a group from the given index.
     *
     * @param index index of the group
     * @return      position of the group; -1 if it does not exist.
     */
    public static int getPosition(int index) {
        if ((index < 0) || (index >= groupLength)) {
            return -1;
        }

        Group group;

        for (int i = Math.min(groupsList.size(), index) - 1; i >= 0; i--) {
            group = groupsList.get(i);
            if (group != null) {
                if (group.getIndex() == index) {
                    return i;
                }
                else if (group.getIndex() < index) {
                    break;
                }
            }
        }

        return -1;
    }

    /**
     * Clears the list of groups.
     */
    public static void clearGroups() {
        groupLength = 0;
        groups = new Group[initialGroupLength];
        groupsList = new ArrayList<>();
    }

    /**
     * Gets the number of groups in the list.
     *
     * @return          the number of groups in the list.
     */
    public static int getGroupSize() {
        return groupsList.size();
    }

    /**
     * Fetches the group with the given group id
     *
     * @param groupID   the group id of the requested group.
     * @return          the requested group; null if it does not exist
     */
    public static Group getGroup(GroupID groupID) {
        for (Group group : groupsList) {
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
        if ((index < 0) || (index >= groupLength)) {
            return null;
        }
        else {
            return groups[index];
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
        if ((groupIndex < 0) || (groupIndex >= groupLength)) {
            return null;
        }
        return groups[groupIndex].getModule(moduleIndex);
    }

    /**
     * Fetches the requested module
     *
     * @param address   the address of the module
     * @return          the requested module; null if it does not exist
     */
    public static Module getModule(int[] address) {
        if ((address.length != 2) || (address[0] < 0) || (address[0] >= groupLength)) {
            return null;
        }
        return groups[address[0]].getModule(address[1]);
    }

    /**
     * Adds the given group to the group list. Sets the group's index to the new index
     *
     * @param group Group to add
     */
    public static void addGroup(Group group) {
        if (groupLength == groups.length) {
            Group[] newGroup = new Group[groups.length * 2];
            for (int i = 0; i < groupLength; i++) {
                newGroup[i] = groups[i];
            }
            groups = newGroup;
        }
        groups[groupLength] = group;
        groupsList.add(group);
        group.setIndex(groupLength);
        groupLength++;
    }

    /**
     * Removes the given group from the list.
     *
     * @param group     group to remove
     */
    public static void deleteGroup(Group group) {
        deleteGroup(group.getIndex());
    }

    /**
     * Removes the group with the given ID from the list.
     *
     * @param groupID   group id of the group to remove
     */
    public static void deleteGroup(GroupID groupID) {
        for (int i = 0; i < groupLength; i++) {
            if ((groups[i] != null) && (groupID.equals(groups[i].getGroupId()))) {
                deleteGroup(groups[i]);
                return;
            }
        }
    }

    /**
     * Deletes the group with the given index
     *
     * @param index     index of the group to remove
     */
    public static void deleteGroup(int index) {
        deleteFromPos(getPosition(index));
    }

    /**
     * Removes a group from its position in the list.
     *
     * @param position  the position of the group
     */
    public static void deleteFromPos(int position) {
        if ((position >= 0) && (position < groupsList.size())) {
            groups[groupsList.get(position).getIndex()] = null;
            groupsList.remove(position);
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

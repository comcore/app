package com.gmail.comcorecrew.comcore.classes;

import android.content.Context;

import com.gmail.comcorecrew.comcore.MainActivity;
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.modules.Calendar;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.exceptions.StorageFileDisjunctionException;
import com.gmail.comcorecrew.comcore.notifications.NotificationScheduler;
import com.gmail.comcorecrew.comcore.server.LoginToken;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.EventEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

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
    private static File appStorage;
    private static LoginToken token;
    private static Group[] groups; //Array containing the groups
    private static ArrayList<Group> groupsList; //Arraylist containing the groups
    //Lists are separate as to allow quick fetching of data
    private static int[] positions;
    private static int groupLength; //Number of groups in the list
    private static final int initialGroupLength = 10;
    public static final int maxData = 0x001E8483; //4MB + 6 Bytes of chars
    public static final int cacheVersion = 4;

    /**
     * Runs functions to read stored data from the app before initialization.
     *
     * @param context       App context
     * @throws IOException  if an IO error occurs
     */
    public static void preInit(Context context, MainActivity activity) throws IOException {
        clearGroups();
        appStorage = new File(context.getFilesDir(), "main");
        if (appStorage.createNewFile()) {
            updateCache(context);
        }
        else {
            FileReader reader = new FileReader(appStorage);
            int curVersion = readInt(reader);
            String userId = readString(reader);
            UserInfo info = null;
            LoginToken token = null;
            if (userId != null) {
                UserID id = new UserID(userId);
                token = new LoginToken(id, readString(reader));
                info = new UserInfo(id, readString(reader));
            }
            reader.close();
            if (curVersion < cacheVersion) {
                updateCache(context);
            }
            if (info != null && token != null)  {
                activity.whenExistingLogin(info, token);
            }
        }
    }

    /**
     * Init method that should be run when app is opened.
     *
     * @param user the user's info
     * @param loginToken the login token
     * @param context App context
     */
    public static void init(UserInfo user, LoginToken loginToken, Context context) throws IOException {
        // Store the user's information first in case there are any errors during initialization
        token = loginToken;
        if (!appStorage.exists()) {
            throw new IOException("Illegal init() call!");
        }

        int version;
        try (FileReader reader = new FileReader(appStorage)) {
            version = readInt(reader);
        }

        try (PrintWriter writer = new PrintWriter(appStorage)) {
            writeInt(version, writer);
            writeString(token.user.id, writer);
            writeString(token.token, writer);
            writeString(user.name, writer);
        }

        self = new User(user);
        self.setInternalId(0);
        cacheDir = new File(context.getCacheDir(), self.getID().id);
        filesDir = new File(context.getFilesDir(), self.getID().id);
        boolean madeDir = filesDir.mkdir();

        UserStorage.init();
        if ((madeDir) && (!UserStorage.addUser(self))) {
            throw new StorageFileDisjunctionException("Impossible use storage state.");
        }
        boolean cacheExists = cacheDir.exists();
        if (!cacheExists) {
            NotificationScheduler.clearAllAlarms();
            if (!cacheDir.mkdir()) {
                throw new IOException("Cannot create cache directory");
            }
        }
        groupsDir = new File(filesDir, "groups");
        if (!groupsDir.mkdir()) {
            GroupStorage.readAllGroups();
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
     * Fetches the latest login token saved.
     *
     * @return last used login token
     */
    public static LoginToken getToken() {
        return token;
    }

    /**
     * Creates a sub group containing the users given
     *
     * @param parent    Group to create the subgroup from
     * @param name      name of the subgroup
     * @param users     users to add to the subgroup
     */
    public static void createSubGroup(Group parent, String name, ArrayList<User> users) {
        ArrayList<UserID> ids = new ArrayList<>();
        for (User user : users) {
            ids.add(user.getID());
        }
        ServerConnector.createSubGroup(parent.getGroupId(), name, ids, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }

            Group group = new Group(name, result.data, GroupRole.OWNER, false);
            addGroup(group);
            try {
                GroupStorage.storeGroup(group);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
            if (groupID.id.equals(groupsList.get(i).getGroupId().id)) {
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
        return positions[index];
    }

    /**
     * Clears the list of groups.
     */
    public static void clearGroups() {
        groupLength = 0;
        groups = new Group[initialGroupLength];
        positions = new int[initialGroupLength];
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
            int[] newPositions = new int[groupLength * 2];
            Group[] newGroup = new Group[groupLength * 2];
            for (int i = 0; i < groupLength; i++) {
                newGroup[i] = groups[i];
                newPositions[i] = positions[i];
            }
            positions = newPositions;
            groups = newGroup;
        }
        groups[groupLength] = group;
        groupsList.add(group);
        group.setIndex(groupLength);
        groupLength++;
        normalizeGroupList();
    }

    /**
     * Adds the given group to the group list. Sets the group's index to the new index
     * Does not sort the group list at the end.
     *
     * @param group Group to add
     */
    public static void quickAddGroup(Group group) {
        if (groupLength == groups.length) {
            int[] newPositions = new int[groupLength * 2];
            Group[] newGroup = new Group[groupLength * 2];
            for (int i = 0; i < groupLength; i++) {
                newGroup[i] = groups[i];
                newPositions[i] = positions[i];
            }
            positions = newPositions;
            groups = newGroup;
        }
        groups[groupLength] = group;
        positions[groupLength] = groupsList.size();
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
        if (group != null) {
            deleteGroup(group.getIndex());
        }
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
            Group group = groupsList.get(position);
            group.onDeleted();
            deleteDirectory(new File(groupsDir, group.getGroupId().id));
            deleteDirectory(new File(cacheDir, group.getGroupId().id));
            groups[group.getIndex()] = null;
            positions[group.getIndex()] = -1;
            groupsList.remove(position);
            for (int i = position; i < groupsList.size(); i++) {
                positions[groupsList.get(i).getIndex()] = i;
            }
        }
    }

    /**
     * Sorts the groupList and updates each position.
     */
    public static void normalizeGroupList() {
        Collections.sort(groupsList);
        for (int i = 0; i < groupsList.size(); i++) {
            positions[groupsList.get(i).getIndex()] = i;
        }
    }

    /**
     * Returns a list of events within the next week from the group.
     *
     * @param group     group to get the events from
     * @return          list of events within the next week
     */
    public static ArrayList<EventEntry> getUpcoming(Group group) {
        ArrayList<EventEntry> upcoming = new ArrayList<>();
        long now = new Date().getTime();
        long then = now + 604800000; //milliseconds in a week
        for (Module module : group.getModules()) {
            if (module instanceof Calendar) {
                for (EventEntry entry : ((Calendar) module).getApproved()) {
                    if (entry.start >= now && entry.end <= then) {
                        upcoming.add(entry);
                    }
                }
            }
        }
        return upcoming;
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
        if (length < 0) {
            return null;
        }
        char[] buf = new char[length];
        if (reader.read(buf) != length) {
            throw new IOException();
        }
        return String.copyValueOf(buf);
    }

    public static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }

    public static void clearDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                clearDirectory(file);
                file.delete();
            }
        }
    }

    public static void updateCache(Context context) throws IOException {
        File appStorage = new File(context.getFilesDir(), "main");
        if (!appStorage.exists()) {
            throw new IOException("Illegal updateCache() call");
        }
        clearDirectory(context.getCacheDir());
        RandomAccessFile file = new RandomAccessFile(appStorage, "rw");
        file.seek(0);
        file.write((char) (cacheVersion >> 16));
        file.write((char) cacheVersion);
        file.close();
    }

    /**
     * Clear the stored login token and user data when the user's token expires.
     */
    public static void clearToken() throws IOException {
        int version;
        try (FileReader reader = new FileReader(appStorage)) {
            version = readInt(reader);
        }

        try (PrintWriter writer = new PrintWriter(appStorage)) {
            writeInt(version, writer);
        }
    }
}

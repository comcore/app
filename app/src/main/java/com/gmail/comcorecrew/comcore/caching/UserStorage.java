package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.exceptions.InvalidFileFormatException;
import com.gmail.comcorecrew.comcore.exceptions.StorageFileDisjunctionException;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Static class used to store user data into files.
 * Must call init before running.
 */
public class UserStorage {
    private UserStorage() {}

    private static ArrayList<User> userList; //List of users to fetch from
    private static uNode idList; //Sorted BST of internal ids

    /**
     * Initiates the user storage to be used by the app.
     * NOTE: This MUST be run before any use of the user storage.
     *
     * @throws IOException if an IO error occurs
     */
    public static void init() throws IOException {
        if ((!refreshLists())) {
            // TODO: Prompt user information to add to storage.
        }
    }

    /**
     * Look up the User corresponding to a UserID, or fetch it from the server if it is not
     * already in the UserStorage. If it succeeds, the callback will be called with the retrieved
     * information about the user.
     *
     * @param id       the ID of the group
     * @param callback what to do with the result
     */
    public static void lookup(UserID id, LookupCallback<User> callback) {
        // Try to find the user in the UserStorage
        int internalId = UserStorage.getInternalId(id);
        if (internalId != -1) {
            callback.accept(UserStorage.getUser(internalId));
            return;
        }

        // Otherwise, get the info from the server and cache it
        ServerConnector.getUserInfo(id, 0, result -> {
            if (result.isFailure() || result.data == null) {
                return;
            }

            try {
                User user = new User(id, result.data.name);
                if (UserStorage.addUser(user)) {
                    callback.accept(user);
                } else {
                    callback.accept(UserStorage.getUser(id));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Refresh a list of UserIDs, calling the provided Runnable on completion.
     *
     * @param userIds  the UserIDs to refresh (or null)
     * @param callback the callback to run
     */
    public static void refresh(List<UserID> userIds, Runnable callback) {
        // Finish immediately if no users are being refreshed
        if (userIds.isEmpty()) {
            if (callback != null) {
                callback.run();
            }
            return;
        }

        // Get the info of the users from the server
        ServerConnector.getUserInfo(userIds, 0, result -> {
            if (result.isSuccess()) {
                // Create a User object for each user
                ArrayList<User> users = new ArrayList<>();
                for (UserInfo userInfo : result.data) {
                    users.add(new User(userInfo.id, userInfo.name));
                }

                // Try to add all of the User objects to the UserStorage
                try {
                    addUser(users);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Finish by calling the callback
            if (callback != null) {
                callback.run();
            }
        });
    }

    /**
     * Adds the given user to the user storage.
     *
     * @param user User to add
     * @return true if user is added; false if user is already in list
     * @throws IOException if an IO error occurs
     */
    public static boolean addUser(User user) throws IOException {
        ArrayList<User> list = new ArrayList<>();
        list.add(user);
        return addUser(list);
    }

    /**
     * Adds users from the given list of users into the user storage.
     *
     * @param users A list of users to add
     * @return true if every user is added; false if at least one user is already in the list
     * @throws IOException if an IO error occurs
     */
    public static boolean addUser(ArrayList<User> users) throws IOException {
        File userFile = new File(AppData.filesDir, "userList");
        if (!userFile.exists()) {
            throw new StorageFileDisjunctionException();
        }
        boolean allAdded = true;
        PrintWriter writer = new PrintWriter(new FileWriter(userFile, true));
        for (User user : users) {
            if (!addUserNode(idList, user, writer)) {
                allAdded = false;
            }
        }
        writer.close();
        saveIdList();
        return allAdded;
    }

    /**
     * Returns the user containing the given userID from the user list.
     *
     * @param userID UserID of the requested user.
     * @return the requested user; null if user is not in list
     */
    public static User getUser(UserID userID) {
        return getUser(getInternalId(userID));
    }

    /**
     * Returns the user containing the given internalId from the user list.
     *
     * @param internalId internal id of the requested user.
     * @return the requested user; null if user is not in list
     */
    public static User getUser(int internalId) {
        if ((userList == null) || (internalId < 0)) {
            return null;
        }
        else if (internalId >= userList.size()) {
            throw new StorageFileDisjunctionException();
        }
        return userList.get(internalId);
    }

    /**
     * Gets the internal id of the given user id from the list of internal ids.
     *
     * @param userID userID of the requested user
     * @return the internal id corresponding to the given user id
     */
    public static int getInternalId(UserID userID) {
        return getInternalId(idList, userID);
    }

    /**
     * Reloads each list from storage
     * WARNING: DO NOT USE UNLESS YOU ARE SURE THAT YOU NEED TO!
     * The other functions in this class will make sure that the lists are updated
     * when data is changed.
     *
     * @return true if lists already existed, false if lists were created
     * @throws IOException if an IO error occurs
     */
    public static boolean refreshLists() throws IOException {
        boolean isEmpty = refreshUserList();
        if (isEmpty ^ refreshIdList()) {
            throw new StorageFileDisjunctionException();
        }
        return isEmpty;
    }

    /**
     * Reloads user list from storage
     * WARNING: DO NOT USE UNLESS YOU ARE SURE THAT YOU NEED TO!
     * The other functions in this class will make sure that the lists are updated
     * when data is changed.
     *
     * @return true if list already existed, false if list was created
     * @throws IOException if an IO error occurs
     */
    public static boolean refreshUserList() throws IOException {
        File userFile = new File(AppData.filesDir, "userList");

        //Makes sure there is no file funny business occurring
        if (userFile.createNewFile()) {
            if ((userList != null) && (userList.size() != 0)) {
                throw new StorageFileDisjunctionException();
            }
            else if (userList == null) {
                userList = new ArrayList<>();
            }
            return false;
        }

        //Creates reader
        userList = new ArrayList<>();
        FileReader reader = new FileReader(userFile);
        User user;
        char[] buffer;
        int length = reader.read();
        while (length != -1) {
            length = (length << 16) | reader.read();
            buffer = new char[length];
            if (reader.read(buffer) != length) {
                throw new InvalidFileFormatException();
            }
            user = fromStorageString(String.copyValueOf(buffer));
            user.setInternalId(userList.size());
            userList.add(user);
            length = reader.read();
        }
        reader.close();
        return true;
    }

    /**
     * Reloads id list from storage
     * WARNING: DO NOT USE UNLESS YOU ARE SURE THAT YOU NEED TO!
     * The other functions in this class will make sure that the lists are updated
     * when data is changed.
     *
     * @return true if list already existed, false if list was created
     * @throws IOException if an IO error occurs
     */
    public static boolean refreshIdList() throws IOException {
        File idFile = new File(AppData.filesDir, "idList");
        if (idFile.createNewFile()) {
            if (idList != null) {
                throw new StorageFileDisjunctionException();
            }
            return false;
        }
        FileReader reader = new FileReader(idFile);
        long fileSize = idFile.length();
        if ((fileSize % 2) == 1) {
            throw new InvalidFileFormatException();
        }
        idList = readNode(new long[] {-1, fileSize / 2}, reader);
        reader.close();
        return true;
    }

    /**
     * Hard saves user list into storage.
     * WARNING: DO NOT USE UNLESS YOU ARE SURE THAT YOU NEED TO!
     * The other functions in this class will make sure that the lists are updated
     * when data is changed.
     *
     * @throws IOException if an IO error occurs
     */
    public static void saveUserList() throws IOException {
        if (userList == null) {
            throw new NullPointerException();
        }
        File userFile = new File(AppData.filesDir, "userList");
        if (userFile.createNewFile()) {
            throw new StorageFileDisjunctionException();
        }

        PrintWriter writer = new PrintWriter(userFile);
        for (User user : userList) {
            writeUser(user, writer);
        }
        writer.close();
    }

    /**
     * Hard saves id list into storage
     * WARNING: DO NOT USE UNLESS YOU ARE SURE THAT YOU NEED TO!
     * The other functions in this class will make sure that the lists are updated
     * when data is changed.
     *
     * @throws IOException if an IO error occurs
     */
    public static void saveIdList() throws IOException {
        File idFile = new File(AppData.filesDir, "idList");
        if ((idFile.createNewFile()) && (idList != null)) {
            throw new StorageFileDisjunctionException();
        } //Throws if a new file had to be made but there was data already in the idList
        if (idList == null) {
            return;
        }
        PrintWriter writer = new PrintWriter(idFile);
        writeNode(idList, writer);
        writer.close();
    }

    /**
     * Helper function used to recursively get the internal id from the BST
     *
     * @param root root node of tree to search
     * @param userID user id to search for
     * @return internal id of requested user; -1 if user is not in list
     */
    private static int getInternalId(uNode root, UserID userID) {
        if (userID == null) {
            throw new IllegalArgumentException();
        }
        if (root == null) {
            return -1;
        }
        User cmpUser = getUser(root.internalId);
        if (cmpUser == null) {
            throw new StorageFileDisjunctionException();
        }
        int ret = userID.id.compareTo(cmpUser.getID().id);

        if (ret < 0) {
            return getInternalId(root.leftChild, userID);
        }
        else if (ret > 0) {
            return getInternalId(root.rightChild, userID);
        }
        else {
            return root.internalId;
        }
    }

    /**
     * Helper function to add a node to the list
     *
     * @param root root node of list to add user to
     * @param user user to add to the list
     * @param writer writer that writes user to storage when space is found
     * @return true if user is added; false if user is already in list
     */
    private static boolean addUserNode(uNode root, User user, PrintWriter writer) {
        if (root == null) {
            user.setInternalId(0);
            idList = new uNode();
            idList.internalId = 0;
            idList.leftChild = null;
            idList.rightChild = null;
            writeUser(user, writer);
            userList.add(user);
            return true;
        }
        User cmpUser = getUser(root.internalId);
        if (cmpUser == null) {
            throw new StorageFileDisjunctionException();
        }
        int ret = user.getID().id.compareTo((cmpUser.getID().id));

        if (ret < 0) {
            if (root.leftChild != null) {
                return addUserNode(root.leftChild, user, writer);
            }
            user.setInternalId(userList.size());
            root.leftChild = new uNode();
            root.leftChild.internalId = user.getInternalId();
            root.leftChild.leftChild = null;
            root.leftChild.rightChild = null;
            writeUser(user, writer);
            userList.add(user);
            return true;
        }
        else if (ret > 0) {
            if (root.rightChild != null) {
                return addUserNode(root.rightChild, user, writer);
            }
            user.setInternalId(userList.size());
            root.rightChild = new uNode();
            root.rightChild.internalId = user.getInternalId();
            root.rightChild.leftChild = null;
            root.rightChild.rightChild = null;
            writeUser(user, writer);
            userList.add(user);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Helper function to read list of internal ids into a BST recursively
     *
     * @param bounds array containing the beginning and end of section to add
     * @param reader reader to read from
     * @return root node of the read section
     * @throws IOException if an IO error occurs
     */
    private static uNode readNode(long[] bounds, FileReader reader) throws IOException {
        if ((bounds[1] - bounds[0]) == 1) {
            return null;
        }
        uNode node = new uNode();
        long mid = (bounds[0] + bounds[1]) / 2;
        node.leftChild = readNode(new long[] {bounds[0], mid}, reader);
        node.internalId = reader.read();
        node.internalId = (node.internalId << 16) | reader.read();
        node.rightChild = readNode(new long[] {mid, bounds[1]}, reader);
        return node;
    }

    /**
     * Helper function to write BST into storage
     *
     * @param root root of the tree to write to storage
     * @param writer writer that writes tree
     */
    private static void writeNode(uNode root, PrintWriter writer) {
        if (root == null) {
            return;
        }
        writeNode(root.leftChild, writer);
        writer.write((char) (root.internalId >> 16));
        writer.write((char) root.internalId);
        writer.flush();
        writeNode(root.rightChild, writer);
    }

    /**
     * Helper to write user info to storage.
     *
     * @param user user to write the info of
     * @param writer writer that writes data
     */
    private static void writeUser(User user, PrintWriter writer) {
        String storage = toStorageString(user);
        int length = storage.length();
        writer.write((char) (length >> 16));
        writer.write((char) length);
        writer.write(storage);
        writer.flush();
    }

    /**
     * Helper to convert user info into string.
     * NOTE: As more user data is added, this function will expand to store
     * the added info
     *
     * @param user user to convert info to string
     * @return string of user info
     */
    private static String toStorageString(User user) {
        String storage = "";
        int length = user.getName().length();
        storage += (char) (length >> 16);
        storage += (char) length;
        storage += user.getName();
        length = user.getID().id.length();
        storage += (char) (length >> 16);
        storage += (char) length;
        storage += user.getID().id;
        return storage;
    }

    /**
     * Helper to read user info from storage string
     * NOTE: As more user data is added, this function will expand to store
     * the added info
     *
     * @param string string of user info
     * @return user with given info
     */
    private static User fromStorageString(String string) {
        int length;
        int i = 0;

        //Parses through string
        length = string.charAt(i);
        i++;
        length = (length << 16) | string.charAt(i);
        i++;
        String userName = string.substring(i, i + length);
        i += length;
        length = string.charAt(i);
        i++;
        length = (length << 16) | string.charAt(i);
        i++;
        String id = string.substring(i, i + length);

        return new User(new UserID(id), userName);
    }

    /**
     * Helper class that contains internal ids in a BTS
     */
    private static class uNode {
        public uNode leftChild;
        public uNode rightChild;
        public int internalId;

        private uNode() {}

    }
}

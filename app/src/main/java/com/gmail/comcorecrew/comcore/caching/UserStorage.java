package com.gmail.comcorecrew.comcore.caching;

import android.content.Context;

import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class UserStorage {

    private static ArrayList<User> userList; //List of users to fetch from
    private static uNode idList; //Sorted BST of internal ids

    /**
     * Returns the User with the specified userID.
     *
     * @param userID userID of the specified user
     * @return Returns null if error or user does not exist
     */
    public static User getUser(UserID userID) {
        return getUser(getInternalId(userID));
    }

    /**
     * User Storage Initiation. Should be called when loading app.
     *
     * @param self The user information of the account.
     * @param context App context
     * @return Returns true if no errors occur. False otherwise.
     */
    public static boolean Init(User self, Context context) {
        //TODO Implement
        return false;
    }

    /**
     * Adds user to the list of user information and saves lists.
     *
     * @param user The user to add
     * @return True if user is added. False if error occurs
     */
    public static boolean addUser(User user) {
        //TODO Implement
        return false;
    }

    /**
     * Adds list of users and saves storage lists.
     *
     * @param users List of users to be added
     * @return True if users are added. False if error occurs
     */
    public static boolean addUser(ArrayList<User> users) {
        //TODO Implement
        return false;
    }

    /**
     *
     * @param internalId internal ID of the specified user
     * @return Requested user or null if DNE
     */
    public static User getUser(int internalId) {
        if ((userList == null) ||
                (internalId < 0) ||
                (internalId > userList.size())) {
            return null;
        }
        return userList.get(internalId);
    }


    public static int getInternalId(UserID userID) {
        return getInternalId(idList, userID);
    }

    public static boolean refreshLists(Context context) {
        return (refreshUserList(context) & refreshIdList(context));
    }

    public static boolean refreshUserList(Context context) {
        //TODO Implement
        return false;
    }

    public static boolean refreshIdList(Context context) {
        try {
            File idFile = new File(context.getFilesDir(), "idList");
            if (idFile.createNewFile()) {
                idList = null;
                return false;
            }
            RandomAccessFile file = new RandomAccessFile(idFile, "r");
            long fileSize = file.length();
            if ((fileSize % 2) == 1) {
                throw new IllegalArgumentException();
            }
            idList = readNode(new long[] {-1, fileSize / 2}, file);
            file.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void saveUserList(Context context) {
        //TODO Implement
    }

    public static void saveIdList(Context context) {
        try {
            File idFile = new File(context.getFilesDir(), "idList");
            if (idFile.createNewFile()) {
                return; //TODO Throw exception
            }
            PrintWriter writer = new PrintWriter(idFile);
            writeNode(idList, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int getInternalId(uNode root, UserID userID) {
        try {
            int ret = userID.id.compareTo(getUser(root.internalId).getID().id);

            if (ret < 0) {
                return getInternalId(root.leftChild, userID);
            }
            else if (ret > 0) {
                return getInternalId(root.rightChild, userID);
            }
            else {
                return root.internalId;
            }
        } catch (NullPointerException e) {
            return -1;
        }
    }

    private static uNode readNode(long[] bounds, RandomAccessFile file) {
        try {
            if ((bounds[1] - bounds[0]) == 1) {
                return null;
            }
            uNode node = new uNode();
            long mid = (bounds[0] + bounds[1]) / 2;
            file.seek(2 * mid);
            node.internalId = file.read();
            node.internalId = (node.internalId << 16) | file.read();
            node.leftChild = readNode(new long[] {bounds[0], mid}, file);
            node.rightChild = readNode(new long[] {mid, bounds[1]}, file);
            return node;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    private static String toStorageString(User user) {
        String storage = "";
        int length = user.getName().length();
        storage += (char) (length >> 16);
        storage += (char) length;
        storage += user.getName();
        length = user.getInternalId();
        storage += (char) (length >> 16);
        storage += (char) length;
        storage += user.getID().id;
        return storage;
    }

    private static User fromStorageString(String string) {
        int length;
        int i = 0;

        length = string.charAt(i);
        i++;
        length = (length << 16) | string.charAt(i);
        i++;
        String userName = string.substring(i, i + length);
        i += length;length = string.charAt(i);
        i++;
        length = (length << 16) | string.charAt(i);
        i++;
        String id = string.substring(i, i + length);
        return new User(new UserID(id), userName);
    }

    private static class uNode {
        public uNode leftChild;
        public uNode rightChild;
        public int internalId;

        private uNode() {}

    }
}

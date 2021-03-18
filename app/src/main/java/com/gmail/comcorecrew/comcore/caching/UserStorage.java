package com.gmail.comcorecrew.comcore.caching;

import android.content.Context;

import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.exceptions.InvalidFileFormatException;
import com.gmail.comcorecrew.comcore.exceptions.StorageFileDisjunctionException;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class UserStorage {

    private static ArrayList<User> userList; //List of users to fetch from
    private static uNode idList; //Sorted BST of internal ids


    public static User getUser(UserID userID) {
        return getUser(getInternalId(userID));
    }

    public static void init(User self, Context context) throws IOException {
        if ((!refreshLists(context)) && !addUser(self, context)) {
            throw new StorageFileDisjunctionException();
        }
    }

    public static boolean addUser(User user, Context context) throws IOException{
        ArrayList<User> list = new ArrayList<>();
        list.add(user);
        return addUser(list, context);
    }

    public static boolean addUser(ArrayList<User> users, Context context) throws IOException {
        File userFile = new File(context.getFilesDir(), "userList");
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
        saveIdList(context);
        return allAdded;
    }

    /**
     *
     * @param internalId internal ID of the specified user
     * @return Returns requested user or null if user does not exist
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


    public static int getInternalId(UserID userID) {
        return getInternalId(idList, userID);
    }

    public static boolean refreshLists(Context context) throws IOException {
        boolean isEmpty = refreshUserList(context);
        if (isEmpty ^ refreshIdList(context)) {
            throw new StorageFileDisjunctionException();
        }
        return isEmpty;
    }

    public static boolean refreshUserList(Context context) throws IOException {
        File userFile = new File(context.getFilesDir(), "userList");

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
        BufferedReader reader = new BufferedReader(new FileReader(userFile));
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

    public static boolean refreshIdList(Context context) throws IOException {
        File idFile = new File(context.getFilesDir(), "idList");
        if (idFile.createNewFile()) {
            if (idList != null) {
                throw new StorageFileDisjunctionException();
            }
            return false;
        }
        RandomAccessFile file = new RandomAccessFile(idFile, "r");
        long fileSize = file.length();
        if ((fileSize % 2) == 1) {
            throw new InvalidFileFormatException();
        }
        idList = readNode(new long[] {-1, fileSize / 2}, file);
        file.close();
        return true;
    }

    public static void saveUserList(Context context) throws IOException {
        if (userList == null) {
            throw new NullPointerException();
        }
        File userFile = new File(context.getFilesDir(), "userList");
        if (userFile.createNewFile()) {
            throw new StorageFileDisjunctionException();
        }

        PrintWriter writer = new PrintWriter(userFile);
        for (User user : userList) {
            writeUser(user, writer);
        }
        writer.close();
    }

    public static void saveIdList(Context context) throws IOException {
        File idFile = new File(context.getFilesDir(), "idList");
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

    private static uNode readNode(long[] bounds, RandomAccessFile file) throws IOException {
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

    private static void writeUser(User user, PrintWriter writer) {
        String storage = toStorageString(user);
        int length = storage.length();
        writer.write((char) (length >> 16));
        writer.write((char) length);
        writer.write(storage);
        writer.flush();
    }

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

    private static User fromStorageString(String string) {
        int length;
        int i = 0;

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

    private static class uNode {
        public uNode leftChild;
        public uNode rightChild;
        public int internalId;

        private uNode() {}

    }
}

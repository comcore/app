package com.gmail.comcorecrew.comcore.classes;

import android.content.Context;

import com.gmail.comcorecrew.comcore.caching.GroupStorage;
import com.gmail.comcorecrew.comcore.caching.UserStorage;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Singleton class to store data and contains helper functions.
 */
public class AppData {

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
    public static void init(Context context) throws IOException {
        cacheDir = context.getCacheDir();
        filesDir = context.getFilesDir();
        UserStorage.init();
        groupsDir = new File(filesDir, "groups");
        if (!groupsDir.mkdir()) {
            GroupStorage.readAllGroups();
        }
        else {
            groups = new ArrayList<>();
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

package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

/*
 * Class for the standard cacheable format. Supports the caching of an int,
 * a long, and a string up to 4 MB long.
 */
public class MsgCacheable implements Cacheable {

    private int id; //Preferably user id of the message.
    private long messageid; //Message id
    private long timestamp; //Meta data for any use.
    private String data; //Data contained in the message.

    public MsgCacheable(MessageEntry entry) {
        id = UserStorage.getInternalId(entry.sender);
        messageid = entry.id.id;
        timestamp = entry.timestamp;
        data = entry.contents;
    }

    public MsgCacheable(int id, long messageid, long timestamp, String data) {
        if (data.length() > (AppData.maxData - 5)) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.messageid = messageid;
        this.timestamp = timestamp;
        this.data = data;
    }

    //Creates a new object from cache array.
    public MsgCacheable(char[] cache) {
        if (cache.length < 10) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        //Reads the array into the object.
        id = cache[0];
        id = (id << 16) | cache[1];
        messageid = cache[2];
        messageid = (messageid << 16) | cache[3];
        messageid = (messageid << 16) | cache[4];
        messageid = (messageid << 16) | cache[5];
        timestamp = cache[6];
        timestamp = (timestamp << 16) | cache[7];
        timestamp = (timestamp << 16) | cache[8];
        timestamp = (timestamp << 16) | cache[9];
        data = new String(cache, 10, cache.length - 10);
    }

    //Creates a new object from cache string.
    public MsgCacheable(String cache) {
        if (cache.length() < 10) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        //Reads the array into the object.
        id = cache.charAt(0);
        id = (id << 16) | cache.charAt(1);
        messageid = cache.charAt(2);
        messageid = (messageid << 16) | cache.charAt(3);
        messageid = (messageid << 16) | cache.charAt(4);
        messageid = (messageid << 16) | cache.charAt(5);
        timestamp = cache.charAt(6);
        timestamp = (timestamp << 16) | cache.charAt(7);
        timestamp = (timestamp << 16) | cache.charAt(8);
        timestamp = (timestamp << 16) | cache.charAt(9);
        data = cache.substring(10);
    }

    @Override
    public char[] toCache() {
        char[] cache = new char[2 + 4 + 4 + data.length()];

        cache[0] = (char) (id >> 16);
        cache[1] = (char) id;
        cache[2] = (char) (messageid >> 48);
        cache[3] = (char) (messageid >> 32);
        cache[4] = (char) (messageid >> 16);
        cache[5] = (char) messageid;
        cache[6] = (char) (timestamp >> 48);
        cache[7] = (char) (timestamp >> 32);
        cache[8] = (char) (timestamp >> 16);
        cache[9] = (char) timestamp;
        for (int i = 0; i < data.length(); i++) {
            cache[10 + i] = data.charAt(i);
        }

        return cache;
    }

    //Reads a char array from cache into the data.
    public void fromCache(char[] cache) {
        if (cache.length < 10) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        //Reads the array into the object.
        id = cache[0];
        id = (id << 16) | cache[1];
        messageid = cache[2];
        messageid = (messageid << 16) | cache[3];
        messageid = (messageid << 16) | cache[4];
        messageid = (messageid << 16) | cache[5];
        timestamp = cache[6];
        timestamp = (timestamp << 16) | cache[7];
        timestamp = (timestamp << 16) | cache[8];
        timestamp = (timestamp << 16) | cache[9];
        data = new String(cache, 10, cache.length - 10);
    }

    public MessageEntry toEntry(ChatID chatID) {
        return new MessageEntry(new MessageID(chatID, messageid),
                UserStorage.getUser(id).getID(), timestamp, data);
    }

    public long getBytes() {
        return 4 + 4 + 8 + 8 + (data.length() * 2);
    }

    //Get and Set methods.
    public int getId() {
        return id;
    }

    public long getMessageid() {
        return messageid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getData() {
        return data;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMessageid(long messageid) {
        this.messageid = messageid;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setData(String data) {
        this.data = data;
    }
}

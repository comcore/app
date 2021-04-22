package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.enums.TaskStatus;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.util.Collections;

public class CustomItem implements Cacheable {

    private int id;
    private int altId;
    private long itemId;
    private long timestamp;
    private int metaInt;
    private long metaLong;
    private String data;

    public CustomItem(MessageEntry entry) {
        id = UserStorage.getInternalId(entry.sender);
        altId = -1;
        itemId = entry.id.id;
        timestamp = entry.timestamp;
        metaInt = -1;
        metaLong = -1;
        data = entry.contents;
    }

    public CustomItem(TaskEntry entry) {
        id = UserStorage.getInternalId(entry.creator);
        itemId = entry.id.id;
        timestamp = entry.timestamp;
        metaLong = entry.deadline;
        metaInt = entry.completer == null ? -1 : UserStorage.getInternalId(entry.completer);
        altId = entry.assigned == null ? -1 : UserStorage.getInternalId(entry.assigned);
        data = entry.description;
    }

    public int getId() {
        return id;
    }

    public int getAltId() {
        return altId;
    }

    public long getItemId() {
        return itemId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getMetaInt() {
        return metaInt;
    }

    public long getMetaLong() {
        return metaLong;
    }

    public boolean isCompleted() {
        return metaInt >= 0;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAltId(int altId) {
        this.altId = altId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public void setMetaInt(int metaInt) {
        this.metaInt = metaInt;
    }

    public void setMetaLong(long metaLong) {
        this.metaLong = metaLong;
    }

    public MessageEntry toEntry(ChatID chatID) {
        MessageID messageID = new MessageID(chatID, itemId);
        return new MessageEntry(messageID, UserStorage.getUser(id).getID(), timestamp, data,
                Collections.emptyMap());
    }

    public TaskEntry toEntry(TaskListID listID) {
        TaskID taskID = new TaskID(listID, itemId);

        return new TaskEntry(taskID, UserStorage.getUser(id).getID(), timestamp, metaLong, data,
                isCompleted() ? UserStorage.getUser(metaInt).getID() : null,
                altId >= 0 ? UserStorage.getUser(altId).getID() : null);
    }

    @Override
    public char[] toCache() {
        char[] cache = new char[charLength()];

        int index = 0;

        cache[index++] = (char) (id >> 16);
        cache[index++] = (char) id;
        cache[index++] = (char) (altId >> 16);
        cache[index++] = (char) altId;
        cache[index++] = (char) (itemId >> 48);
        cache[index++] = (char) (itemId >> 32);
        cache[index++] = (char) (itemId >> 16);
        cache[index++] = (char) itemId;
        cache[index++] = (char) (timestamp >> 48);
        cache[index++] = (char) (timestamp >> 32);
        cache[index++] = (char) (timestamp >> 16);
        cache[index++] = (char) timestamp;
        cache[index++] = (char) (metaInt >> 16);
        cache[index++] = (char) metaInt;
        cache[index++] = (char) (metaLong >> 48);
        cache[index++] = (char) (metaLong >> 32);
        cache[index++] = (char) (metaLong >> 16);
        cache[index++] = (char) metaLong;
        for (int i = 0; i < data.length(); i++) {
            cache[index++] = data.charAt(i);
        }

        return cache;
    }

    public CustomItem(char[] cache) {
        if (cache.length < minLength()) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        int index = 0;

        //Reads the array into the object.
        id = cache[index++];
        id = (id << 16) | cache[index++];
        altId = cache[index++];
        altId = (altId << 16) | cache[index++];
        itemId = cache[index++];
        itemId = (itemId << 16) | cache[index++];
        itemId = (itemId << 16) | cache[index++];
        itemId = (itemId << 16) | cache[index++];
        timestamp = cache[index++];
        timestamp = (timestamp << 16) | cache[index++];
        timestamp = (timestamp << 16) | cache[index++];
        timestamp = (timestamp << 16) | cache[index++];
        metaInt = cache[index++];
        metaInt = (metaInt << 16) | cache[index++];
        metaLong = cache[index++];
        metaLong = (metaLong << 16) | cache[index++];
        metaLong = (metaLong << 16) | cache[index++];
        metaLong = (metaLong << 16) | cache[index++];
        data = new String(cache, index, cache.length - index);
    }

    public static int minLength() {
        int total = 0;
        total += 2; //userId
        total += 2; //altId
        total += 4; //itemId
        total += 4; //timestamp
        total += 2; //metaInt
        total += 4; //metaLong
        return total;
    }

    public int charLength() {
        int total = minLength();
        total += data.length();
        return total;
    }

    public long getBytes() {
        return charLength() * 2;
    }
}
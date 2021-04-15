package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;

public class CustomItem implements Cacheable {

    private int id;
    private long itemId;
    private long timestamp;
    private boolean completed;
    private String data;

    public CustomItem(MessageEntry entry) {
        id = UserStorage.getInternalId(entry.sender);
        itemId = entry.id.id;
        timestamp = entry.timestamp;
        completed = false;
        data = entry.contents;
    }

    public CustomItem(TaskEntry entry) {
        id = UserStorage.getInternalId(entry.creator);
        itemId = entry.id.id;
        timestamp = entry.timestamp;
        completed = entry.completed;
        data = entry.description;
    }

    public int getId() {
        return id;
    }

    public long getItemId() {
        return itemId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isCompleted() {
        return completed;
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

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public MessageEntry toEntry(ChatID chatID) {
        MessageID messageID = new MessageID(chatID, itemId);
        return new MessageEntry(messageID, UserStorage.getUser(id).getID(), timestamp, data);
    }

    public TaskEntry toEntry(TaskListID listID) {
        TaskID taskID = new TaskID(listID, itemId);
        return new TaskEntry(taskID, UserStorage.getUser(id).getID(), timestamp, data, completed);
    }

    @Override
    public char[] toCache() {
        char[] cache = new char[2 + 4 + 4 + 1 + data.length()];

        cache[0] = (char) (id >> 16);
        cache[1] = (char) id;
        cache[2] = (char) (itemId >> 48);
        cache[3] = (char) (itemId >> 32);
        cache[4] = (char) (itemId >> 16);
        cache[5] = (char) itemId;
        cache[6] = (char) (timestamp >> 48);
        cache[7] = (char) (timestamp >> 32);
        cache[8] = (char) (timestamp >> 16);
        cache[9] = (char) timestamp;
        if (completed) {
            cache[10] = 1;
        }
        else {
            cache[10] = 0;
        }
        for (int i = 0; i < data.length(); i++) {
            cache[11 + i] = data.charAt(i);
        }

        return cache;
    }

    public CustomItem(char[] cache) {
        if (cache.length < 11) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        //Reads the array into the object.
        id = cache[0];
        id = (id << 16) | cache[1];
        itemId = cache[2];
        itemId = (itemId << 16) | cache[3];
        itemId = (itemId << 16) | cache[4];
        itemId = (itemId << 16) | cache[5];
        timestamp = cache[6];
        timestamp = (timestamp << 16) | cache[7];
        timestamp = (timestamp << 16) | cache[8];
        timestamp = (timestamp << 16) | cache[9];
        completed = cache[10] != 0;
        data = new String(cache, 11, cache.length - 11);
    }

    public long getBytes() {
        return 4 + 4 + 8 + 8 + 2 + (data.length() * 2);
    }
}
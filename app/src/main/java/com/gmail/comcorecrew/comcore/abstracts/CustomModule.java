package com.gmail.comcorecrew.comcore.abstracts;

import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.CustomItem;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.id.ModuleItemID;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public abstract class CustomModule extends Module {

    private long cacheByteLimit;
    private transient ArrayList<CustomItem> items;

    public CustomModule(String name, CustomModuleID id, Group group) {
        super(name, id, group, Mdid.CSTM);
        cacheByteLimit = -1;
        items = new ArrayList<>();
    }

    public CustomModule(String name, CustomModuleID id, Group group, long cacheByteLimit) {
        super(name, id, group, Mdid.CSTM);
        this.cacheByteLimit = cacheByteLimit;
        items = new ArrayList<>();
    }

    public CustomModule(String name, Group group, String type) {
        super(name, group, type);
        cacheByteLimit = -1;
        items = new ArrayList<>();
    }

    public CustomModule(String name, Group group, String type, long cacheByteLimit) {
        super(name, group, type);
        this.cacheByteLimit = cacheByteLimit;
        items = new ArrayList<>();
    }

    public void setCacheByteLimit(long cacheByteLimit) {
        this.cacheByteLimit = cacheByteLimit;
    }

    @Override
    protected void readToCache() {
        if (items.size() == 0) {
            return;
        }

        ArrayList<Cacheable> cacheItems;

        if (cacheByteLimit < 0) {
            cacheItems = new ArrayList<>(items);
        }
        else {
            cacheItems = new ArrayList<>();
            int index = items.size() - 1;
            long bytes = items.get(index).getBytes();
            index--;
            while ((index >= 0) && ((bytes + items.get(index).getBytes()) < cacheByteLimit)) {
                bytes += items.get(index).getBytes();
                index--;
            }
            index++;

            while (index < items.size()) {
                cacheItems.add(items.get(index));
                index++;
            }
        }

        Cacher.cacheData(cacheItems, this);
    }

    @Override
    protected void readFromCache() {
        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        items = new ArrayList<>();
        for (char[] line : data) {
            items.add(new CustomItem(line));
        }
    }

    protected CustomItem getFirstItem() {
        if (items.isEmpty()) {
            return null;
        }
        return items.get(0);
    }

    protected CustomItem getLastItem() {
        if (items.isEmpty()) {
            return null;
        }
        return items.get(items.size() - 1);
    }

    protected boolean isEmpty() {
        return items.isEmpty();
    }

    protected ChatID getChatID() {
        return ((CustomModuleID) getId()).asChat();
    }

    protected TaskListID getTaskID() {
        return ((CustomModuleID) getId()).asTaskList();
    }

    protected ArrayList<CustomItem> getItems() {
        return items;
    }

    protected void setItems(ArrayList<CustomItem> items) {
        this.items = items;
        toCache();
    }

    protected void addItem(CustomItem item) {
        items.add(item);
        toCache();
    }

    protected void addItems(ArrayList<CustomItem> items) {
        this.items.addAll(items);
        toCache();
    }

    protected void updateItem(MessageEntry entry) {
        long itemId = entry.id.id;
        for (CustomItem item : items) {
            if (item.getItemId() == itemId) {
                item.setId(UserStorage.getInternalId(entry.sender));
                item.setItemId(entry.id.id);
                item.setTimestamp(entry.timestamp);
                item.setData(entry.contents);
                toCache();
                return;
            }
        }
    }

    protected void updateItem(TaskEntry entry) {
        long itemId = entry.id.id;
        for (CustomItem item : items) {
            if (item.getItemId() == itemId) {
                item.setId(UserStorage.getInternalId(entry.creator));
                item.setItemId(entry.id.id);
                item.setTimestamp(entry.timestamp);
                item.setCompleted(entry.completed);
                item.setData(entry.description);
                toCache();
                return;
            }
        }
    }

    protected void sendMessage(String data) {
        ServerConnector.sendMessage(((CustomModuleID) getId()).asChat(), data, result -> {
            if (result.isFailure()) {
                return;
            }

            addItem(new CustomItem(result.data));
        });
    }

    protected void sendTask(String data) {
        ServerConnector.addTask(((CustomModuleID) getId()).asTaskList(), data, result -> {
            if (result.isFailure()) {
                return;
            }

            addItem(new CustomItem(result.data));
        });
    }

    protected void modifyItem(ModuleItemID<ChatID> itemId, String data) {
        ServerConnector.updateMessage((MessageID) itemId, data, result -> {
            if (result.isFailure()) {
                return;
            }

            updateItem(result.data);
        });
    }

    protected void modifyItem(ModuleItemID<TaskListID> itemId, boolean completed) {
        TaskListID taskListID = ((CustomModuleID) getId()).asTaskList();
        ServerConnector.updateTask((TaskID) itemId, completed, result -> {
            if (result.isFailure()) {
                return;
            }

            updateItem(result.data);
        });
    }

    protected void deleteItem(MessageID messageID) {
        ServerConnector.updateMessage(messageID, null, result -> {
            if (result.isFailure()) {
                return;
            }

            updateItem(result.data);
        });
    }

    protected void deleteItem(TaskID taskID) {
        ServerConnector.deleteTask(taskID, result -> {
            if (result.isFailure()) {
                return;
            }

            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getItemId() == taskID.id) {
                    items.remove(i);
                    toCache();
                    return;
                }
            }
        });
    }

    public void onViewCreated(@NonNull View view, Fragment fragment) {
        viewInit(view, fragment);
    }

    abstract public void viewInit(View view, Fragment fragment);

    abstract public int getLayout();

    abstract public void refreshView();

}

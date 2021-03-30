package com.gmail.comcorecrew.comcore.abstracts;

import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.CustomItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;

import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public abstract class CustomModule extends Module {

    private long cacheByteLimit;
    private ArrayList<CustomItem> items;

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
    public void toCache() {
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
    public void fromCache() {
        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        items = new ArrayList<>();
        for (char[] line : data) {
            items.add(new CustomItem(line));
        }
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

    protected void modifyItem(long itemId, String data, long timestamp) {
        for (CustomItem item : items) {
            if (item.getItemId() == itemId) {
                item.setData(data);
                item.setTimestamp(timestamp);
                toCache();
                return;
            }
        }
    }

    protected void modifyItem(long itemId, boolean completed, long timestamp) {
        for (CustomItem item : items) {
            if (item.getItemId() == itemId) {
                item.setCompleted(completed);
                item.setTimestamp(timestamp);
                toCache();
                return;
            }
        }
    }

    protected void sendItem(String data) {
        ServerConnector.sendMessage(((CustomModuleID) getId()).asChat(), data, result -> {
            if (result.isFailure()) {
                //TODO Implement failure
            }
        });
    }

    abstract public void viewInit(@NonNull View view);

    abstract public int getLayout();

}

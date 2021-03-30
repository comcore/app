package com.gmail.comcorecrew.comcore.abstracts;

import com.gmail.comcorecrew.comcore.caching.CustomItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;

import java.util.ArrayList;

public abstract class CustomChat extends CustomModule {

    public CustomChat(String name, CustomModuleID id, Group group) {
        super(name, id, group);
    }

    public CustomChat(String name, CustomModuleID id, Group group, long cacheByteLimit) {
        super(name, id, group, cacheByteLimit);
    }

    public CustomChat(String name, Group group, String type) {
        super(name, group, type);
    }

    public CustomChat(String name, Group group, String type, long cacheByteLimit) {
        super(name, group, type, cacheByteLimit);
    }

    protected ArrayList<MessageEntry> getMessages() {
        ArrayList<MessageEntry> messages = new ArrayList<>();
        ChatID fauxId = ((CustomModuleID) getId()).asChat();
        for (CustomItem item : getItems()) {
            messages.add(item.toEntry(fauxId));
        }
        return messages;
    }

    protected void setMessages(ArrayList<MessageEntry> messages) {
        ArrayList<CustomItem> items = new ArrayList<>();
        for (MessageEntry message : messages) {
            items.add(new CustomItem(message));
        }
        setItems(items);
    }

    protected void editMessage(MessageID messageID, String data, long timestamp) {
        modifyItem(messageID.id, data, timestamp);
    }

    protected void addMessage(MessageEntry message) {
        addItem(new CustomItem(message));
    }

    protected void addMessages(ArrayList<MessageEntry> messages) {
        ArrayList<CustomItem> items = new ArrayList<>();
        for (MessageEntry message : messages) {
            items.add(new CustomItem(message));
        }
        addItems(items);
    }

    protected void sendMessage(String message) {

    }
}

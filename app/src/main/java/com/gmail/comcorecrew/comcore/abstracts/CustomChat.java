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

    public CustomChat(String name, Group group) {
        super(name, group);
    }

    public CustomChat(String name, Group group, long cacheByteLimit) {
        super(name, group, cacheByteLimit);
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

    protected void editMessage(MessageID messageID, String data) {
        modifyItem(messageID, data);
    }

    protected void editMessage(MessageEntry message, String data) {
        modifyItem(message.id, data);
    }

    protected void deleteMessage(MessageID messageID) {
        deleteItem(messageID);
    }

    protected void deleteMessage(MessageEntry message) {
        deleteItem(message.id);
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

    private MessageID latestMessageId() {
        if (!isEmpty()) {
            return new MessageID(getChatID(), getLastItem().getItemId());
        } else {
            return null;
        }
    }

    @Override
    public void refresh() {
        ServerConnector.getMessages(getChatID(), latestMessageId(), null, result -> {
            if (result.isFailure()) {
                return;
            }

            ArrayList<CustomItem> items = new ArrayList<>();

            for (MessageEntry entry : result.data) {
                items.add(new CustomItem(entry));
            }

            // If the message isn't immediately after the existing messages, clear the cache
            if (result.data.length > 0 && !result.data[0].id.immediatelyAfter(latestMessageId())) {
                setItems(items);
            } else {
                addItems(items);
            }

        });
    }

    @Override
    public void clearCache() {
        getItems().clear();
    }

    @Override
    public void onReceiveMessage(MessageEntry message) {
        if (!message.id.module.equals(getId())) {
            return;
        }

        addMessage(message);
    }

    @Override
    public void onMessageUpdated(MessageEntry message) {
        if (!message.id.module.equals(getId())) {
            return;
        }

        updateItem(message);
    }
}

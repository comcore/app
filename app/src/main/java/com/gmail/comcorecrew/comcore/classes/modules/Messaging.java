package com.gmail.comcorecrew.comcore.classes.modules;


import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.MessageItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;

import java.util.ArrayList;

public class Messaging extends Module {

    public transient ArrayList<MessageItem> messages; //Messages

    public Messaging(String name, ChatID id, Group group) {
        super(name, id, group, Mdid.CMSG);
        messages = new ArrayList<>();
    }

    public Messaging(String name, Group group) {
        super(name, group, Mdid.CMSG);
        messages = new ArrayList<>();
    }

    @Override
    protected void readToCache() {
        if (messages.size() == 0) {
            return;
        }

        int index = messages.size() - 1;
        long bytes = messages.get(index).getBytes();
        long byteGoal = 64000;
        index--;
        while ((index >= 0) && ((bytes + messages.get(index).getBytes()) < byteGoal)) {
            bytes += messages.get(index).getBytes();
            index--;
        }
        index++;

        ArrayList<Cacheable> cacheMessages = new ArrayList<>();
        while (index < messages.size()) {
            cacheMessages.add(messages.get(index));
            index++;
        }

        Cacher.cacheData(cacheMessages, this);
    }

    @Override
    protected void readFromCache() {
        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        messages = new ArrayList<>();
        for (char[] line : data) {
            messages.add(new MessageItem(line));
        }
    }

    /*
     * Adds a message and saves the user data.
     */
    private void addMessage(MessageEntry entry) {
        messages.add(new MessageItem(entry));
    }

    public int numEntries() {
        return messages.size();
    }

    public MessageItem get(int i) {
        return messages.get(i);
    }

    public MessageEntry getEntry(int i) {
        return messages.get(i).toEntry((ChatID) getId());
    }

    /*
     * Get the MessageID of the latest message
     */
    public MessageID latestMessageId() {
        if (messages.isEmpty()) {
            return null;
        } else {
            return new MessageID((ChatID) getId(), messages.get(messages.size() - 1).getMessageid());
        }
    }

    /*
     * Gets messages from the server and caches them
     */
    @Override
    public void refresh() {
        ServerConnector.getMessages((ChatID) getId(), latestMessageId(), null, result -> {
            if (result.isFailure()) {
                return;
            }

            // If the message isn't immediately after the existing messages, clear the cache
            if (result.data.length > 0 && !result.data[0].id.immediatelyAfter(latestMessageId())) {
                clearCache();
            }

            for (MessageEntry entry : result.data) {
                addMessage(entry);
            }

            this.toCache();
        });
    }

    @Override
    public void clearCache() {
        messages.clear();
    }

    @Override
    public void onReceiveMessage(MessageEntry message) {
        if (!message.id.module.equals(getId())) {
            return;
        }

        // Don't add it directly if the cache is empty, but refresh the whole chat instead
        MessageID lastMessageId = latestMessageId();
        if (lastMessageId == null || !message.id.immediatelyAfter(lastMessageId)) {
            refresh();
            return;
        }

        addMessage(message);
        this.toCache();
    }

    /**
     * Try to get the message in constant time by doing some indexing tricks. If there is an issue,
     * clear the cache and refresh. Returns null if the message couldn't be retrieved.
     *
     * @param message the message to search for
     * @return the MessageItem
     */
    private MessageItem fastLookupMessage(MessageID message) {
        if (messages.isEmpty() || !message.module.equals(getId())) {
            return null;
        }

        long id = message.id;
        long index = id - messages.get(0).getMessageid();
        if (index >= 0) {
            if (index < messages.size()) {
                MessageItem msg = messages.get((int) index);
                if (msg.getMessageid() == id) {
                    return msg;
                }

                clearCache();
            }

            refresh();
        }

        return null;
    }

    @Override
    public void onMessageUpdated(MessageEntry message) {
        MessageItem msg = fastLookupMessage(message.id);
        if (msg == null) {
            return;
        }

        msg.setTimestamp(message.timestamp);
        msg.setData(message.contents);
        msg.setReactions(message.reactions);
        toCache();
    }

    public void createPinnedMessages(String name) {
        for (Module m : getGroup().getModules()) {
            if ((m instanceof PinnedMessages) &&
                    (((PinnedMessages) m).getChatId().equals(getId().id))) {
                return;
            }
        }
        new PinnedMessages(name, getGroup(), (ChatID) getId());
    }
}

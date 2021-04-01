package com.gmail.comcorecrew.comcore.classes.modules;


import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.MsgCacheable;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Messaging extends Module {

    public transient ArrayList<MsgCacheable> messages; //Messages

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
            messages.add(new MsgCacheable(line));
        }
    }

    /*
     * Adds a message and saves the user data.
     */
    public boolean addMessage(MessageEntry entry) {
        int internalId = UserStorage.getInternalId(entry.sender);
        if (internalId == -1) {/*
            try {
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }*/
            return false; //TODO remove temporary handling.
        }
        MsgCacheable newMsg = new MsgCacheable(internalId, entry.id.id,
                entry.timestamp, entry.contents);
        messages.add(newMsg);
        return true;
    }

    /*
     * Returns a list of messages in oldest first order.
     */
    public ArrayList<MessageEntry> getEntries() {
        ArrayList<MessageEntry> entries = new ArrayList<>();
        for (MsgCacheable msg : messages) {
            entries.add(new MessageEntry(new MessageID((ChatID) getId(), msg.getMessageid()),
                    UserStorage.getUser(msg.getId()).getID(),
                    msg.getTimestamp(), msg.getData()));
        }
        return entries;
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
    public void refreshMessages() {
        ServerConnector.getMessages((ChatID) getId(), latestMessageId(), null, result -> {
            if (result.isFailure()) {
                return;
            }

            // If the message isn't immediately after the existing messages, clear the cache
            if (result.data.length > 0 && !result.data[0].id.immediatelyAfter(latestMessageId())) {
                messages.clear();
            }

            for (MessageEntry entry : result.data) {
                addMessage(entry);
            }

            this.toCache();
        });
    }

    @Override
    public void onReceiveMessage(MessageEntry message) {
        MessageID lastMessageId = latestMessageId();
        if (!message.id.immediatelyAfter(lastMessageId)) {
            messages.clear();
        }
        addMessage(message);
        this.toCache();
    }

    public void deleteMessage(MessageID messageID) {
        for (int i = 0; i < messages.size(); i++) {
            if (messageID.id == messages.get(i).getId()) {
                messages.remove(i);
                break;
            }
        }
        this.toCache();
    }

    public void editMessage(MessageID messageID, String newMsg) {
        for (int i = 0; i < messages.size(); i++) {
            if (messageID.id == messages.get(i).getId()) {
                messages.get(i).setData(newMsg);
                break;
            }
        }
        this.toCache();
    }

    public boolean pinMessage(MessageID messageID) {
        for (Module m : getGroup().getModules()) {
            if ((m instanceof PinnedMessages) &&
                    (((PinnedMessages) m).getChatId().equals(getId().id))) {

                return true;
            }
        }
        return false;
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

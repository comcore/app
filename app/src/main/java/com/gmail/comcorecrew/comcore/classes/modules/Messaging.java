package com.gmail.comcorecrew.comcore.classes.modules;


import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.MsgCacheable;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.interfaces.Module;
import com.gmail.comcorecrew.comcore.server.NotificationListener;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

public class Messaging implements Module {
    private String name; //Name of chat
    private Group group; //Group that the chat is in
    private int mnum; //Module number
    private ChatID id;
    public boolean muted;
    public ArrayList<MsgCacheable> messages; //Messages

    public Messaging() {
        // Empty Module
    }

    public Messaging(String name, ChatID id, Group group) {
        this.group = group;
        this.name = name;
        this.id = id;
        this.mnum = group.addModule(this);
        muted = false;
        messages = new ArrayList<>();
    }

    @Override
    public Mdid getMdid() {
        return Mdid.CMSG;
    }

    @Override
    public String getGroupId() {
        return group.getGroupId().id;
    }

    @Override
    public boolean getMuted() {
        return muted;
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public int getMnum() {
        return mnum;
    }

    public ChatID getChatId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void toCache() {
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
    public void fromCache() {
        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        messages = new ArrayList<>();
        for (char[] line : data) {
            messages.add(new MsgCacheable(line));
        }
    }

    @Override
    public void toFile() {
        try {
            File storage = new File(AppData.filesDir,
                    getGroupId() + "/" + getMdid().toString() + mnum);
            if ((!storage.exists()) && (!storage.createNewFile())) {
                throw new IOException();
            }
            PrintWriter writer = new PrintWriter(storage);

            AppData.writeString(name, writer);
            AppData.writeString(id.id, writer);
            AppData.writeBool(muted, writer);

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromFile(File file, Group group) {
        try {
            String filename = file.getName();
            mnum = Integer.parseInt(filename.substring(4));

            BufferedReader reader = new BufferedReader(new FileReader(file));

            name = AppData.readString(reader);
            id = new ChatID(group.getGroupId(), AppData.readString(reader));
            muted = AppData.readBool(reader);

            reader.close();

            this.group = group;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * Adds a message and saves the user data.
     */
    public boolean addMessage(MessageEntry entry) {
        int internalId = UserStorage.getInternalId(entry.sender.id);
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
            entries.add(new MessageEntry(new MessageID(id, msg.getMessageid()),
                    UserStorage.getUser(msg.getId()).toUserEntry(),
                    msg.getTimestamp(), msg.getData()));
        }
        return entries;
    }

    /*
     * Get the MessageID of the latest message
     */
    private MessageID latestMessageId() {
        if (messages.isEmpty()) {
            return null;
        } else {
            return new MessageID(id, messages.get(messages.size() - 1).getMessageid());
        }
    }

    /*
     * Gets messages from the server and caches them
     */
    public void refreshMessages() {
        MessageID lastMessageId = latestMessageId();
        ServerConnector.getMessages(id, lastMessageId, null, result -> {
            if (result.isFailure()) {
                return;
            }

            // If the message isn't immediately after the existing messages, clear the cache
            if (result.data.length > 0 && !result.data[0].id.immediatelyAfter(lastMessageId)) {
                messages.clear();
            }

            for (MessageEntry entry : result.data) {
                addMessage(entry);
            }
        });
        this.toCache();
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

    @Override
    public void onInvitedToGroup(GroupInviteEntry invite) {}

    @Override
    public void onRoleChanged(GroupID group, GroupRole role) {}

    @Override
    public void onMuteChanged(GroupID group, boolean muted) {}

    @Override
    public void onKicked(GroupID group) {}

    @Override
    public void onLoggedOut() {}

    @Override
    public Collection<? extends NotificationListener> getChildren() {
        return null;
    }
}

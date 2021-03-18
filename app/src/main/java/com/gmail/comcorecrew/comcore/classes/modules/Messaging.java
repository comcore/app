package com.gmail.comcorecrew.comcore.classes.modules;

import android.content.Context;

import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.MsgCacheable;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.caching.StdCacheable;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.interfaces.Module;
import com.gmail.comcorecrew.comcore.server.NotificationListener;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.connection.Message;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.entry.UserEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;

import java.util.ArrayList;
import java.util.Collection;

public class Messaging implements Module {
    private Context context;
    private String name; //Name of chat
    //private final Group group; //Group that the chat is in
    private final int mnum; //Module number
    private ChatID id;
    private GroupID groupID; //temp
    public ArrayList<MsgCacheable> messages; //Messages

    private ArrayList<UserEntry> users;

    public Messaging(Context context, String name, ChatID id) {
        this.context = context;
        //this.group = group;
        this.name = name;
        this.id = id;
        this.mnum = 0; //temp
        this.groupID = id.group; //temp
        //this.mnum = group.addModule(this);
        messages = new ArrayList<>();
        users = new ArrayList<>();
    }

    //Temp function for saving users. WARNING! Has bad search time.
    public int getIndex(UserEntry user) {
        int index = 0;
        while (index < users.size()) {
            if (user.id.equals(users.get(index).id)) {
                return index;
            }
            index++;
        }
        users.add(user);
        return index;
    }

    @Override
    public String getMdid() {
        return "cmsg";
    }

    @Override
    public String getGroupId() {
        //return group.getGroupId().id;
        return groupID.id;
    }

    public int getMnum() {
        return mnum;
    }

    public ChatID getChatId() {
        return id;
    }

    public GroupID getGroup() {
    //public Group getGroup() {
        //return group;
        return groupID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean toCache(Context context) {
        if (messages.size() == 0) {
            return true;
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

        return Cacher.cacheData(cacheMessages, this, context);
    }

    @Override
    public boolean fromCache(Context context) {
        char[][] data = Cacher.uncacheData(this, context);
        if (data == null) {
            return false;
        }

        messages = new ArrayList<>();
        for (char[] line : data) {
            messages.add(new MsgCacheable(line));
        }
        return true;
    }

    /*
     * Adds a message and saves the user data.
     */
    public boolean addMessage(MessageEntry entry) {
        MsgCacheable newMsg = new MsgCacheable(getIndex(entry.sender), entry.id.id,
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
            entries.add(new MessageEntry(new MessageID(id, msg.getMessageid()), users.get(msg.getId()),
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
        this.toCache(context);
    }

    @Override
    public void onReceiveMessage(MessageEntry message) {
        MessageID lastMessageId = latestMessageId();
        if (!message.id.immediatelyAfter(lastMessageId)) {
            messages.clear();
        }
        addMessage(message);
        this.toCache(context);
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

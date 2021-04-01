package com.gmail.comcorecrew.comcore.classes.modules;

import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.abstracts.CustomChat;
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.caching.CustomItem;
import com.gmail.comcorecrew.comcore.caching.MsgCacheable;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.ArrayList;

public class PinnedMessages extends CustomChat {

    private String chatId;
    private transient ArrayList<MessageEntry> pinned;

    public PinnedMessages(String name, CustomModuleID id, Group group, ChatID chat) {
        super(name, id, group);
        chatId = chat.id;
        pinned = new ArrayList<>();
    }

    public PinnedMessages(String name, Group group, ChatID chat) {
        super(name, group, "pinnedMessages");
        chatId = chat.id;
        pinned = new ArrayList<>();
    }

    public static void pinUnpinMessage(MessageEntry message) {
        ChatID chatID = message.id.module;
        GroupID groupID = chatID.group;
        String name = "ERROR";
        for (Group group : AppData.groups) {
            if (group.getGroupId().equals(groupID)) {
                for (Module module : group.getModules()) {
                    if ((module instanceof PinnedMessages) &&
                            (((PinnedMessages) module).chatId.equals(chatID.id))) {
                        ((PinnedMessages) module).pinMessage(message);
                        return;
                    }
                    else if (module.getId().id.equals(chatID.id)) {
                        name = "Pinned" + module.getName();
                    }
                }
                PinnedMessages newPinned = new PinnedMessages(name, group, chatID);
                newPinned.pinMessage(message);
            }
        }
    }

    public String getChatId() {
        return chatId;
    }

    public boolean isPinned(MessageEntry message) {
        MsgCacheable cache;
        for (MessageEntry messageEntry : getMessages()) {
            cache = new MsgCacheable(messageEntry);
            if (message.id.id == cache.getMessageid()) {
                return true;
            }
        }
        return false;
    }

    public void pinMessage(MessageEntry message) {
        if (!isPinned(message)) {
            sendMessage(String.copyValueOf(new MsgCacheable(message).toCache()));
        }
        else {
            //TODO Delete pin
        }
    }

    public void readPinned() {
        ChatID chId = new ChatID(getGroup().getGroupId(), chatId);
        pinned = new ArrayList<>();
        for (CustomItem item : getItems()) {
            pinned.add(new MsgCacheable(item.getData()).toEntry(chId));
        }
    }

    public ArrayList<MessageEntry> getPinned() {
        return pinned;
    }

    @Override
    public void viewInit(@NonNull View view) {
        //TODO Implement
    }

    @Override
    public int getLayout() {
        return -1; //TODO Implement
    }

}

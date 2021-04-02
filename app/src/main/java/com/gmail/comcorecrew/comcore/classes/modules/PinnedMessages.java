package com.gmail.comcorecrew.comcore.classes.modules;

import android.view.View;

import androidx.annotation.NonNull;

import com.gmail.comcorecrew.comcore.abstracts.CustomChat;
import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.CustomItem;
import com.gmail.comcorecrew.comcore.caching.MsgCacheable;
import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.CustomModuleID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;
import com.gmail.comcorecrew.comcore.server.id.TaskID;

import java.util.ArrayList;

public class PinnedMessages extends CustomChat {

    private String chatId;

    public PinnedMessages(String name, CustomModuleID id, Group group, ChatID chat) {
        super(name, id, group);
        if (chat == null) {
            chatId = null;
            refreshChatID();
        }
        else {
            chatId = chat.id;
        }
    }

    public PinnedMessages(String name, Group group, ChatID chat) {
        super(name, group, "pinnedMessages");
        chatId = chat.id;
    }

    /**
     * Attempts to pin/unpin the given message
     *
     * @param message the message to pin
     * @return true if the message status was changed; false if no pinned module exists.
     */
    public static boolean pinUnpinMessage(MessageEntry message) {
        ChatID chatID = message.id.module;
        GroupID groupID = chatID.group;
        for (Group group : AppData.groups) {
            if (group.getGroupId().equals(groupID)) {
                for (Module module : group.getModules()) {
                    if ((module instanceof PinnedMessages) &&
                            (((PinnedMessages) module).chatId.equals(chatID.id))) {
                        ((PinnedMessages) module).pinMessage(message);
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
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
            deleteMessage(message);
        }
    }

    public void refreshChatID() {
        ServerConnector.getTasks(((CustomModuleID) getId()).asTaskList(), result -> {
            if (result.isFailure()) {
                return;
            }
            else if (result.data.length <= 0) {
                getGroup().deleteModule(this);
                return;
            }

            chatId = result.data[0].description;
        });
    }

    public ArrayList<MessageEntry> readPinned() {
        ChatID chId = new ChatID(getGroup().getGroupId(), chatId);
        ArrayList<MessageEntry> pinned = new ArrayList<>();
        for (CustomItem item : getItems()) {
            MsgCacheable message = new MsgCacheable(item.getData());
            if (!message.getData().equals("")) {
                pinned.add(new MsgCacheable(item.getData()).toEntry(chId));
            }
        }
        return pinned;
    }

    @Override
    public void viewInit(@NonNull View view) {
        //TODO Implement
    }

    @Override
    public void refreshView(View view) {

    }

    @Override
    public int getLayout() {
        return -1; //TODO Implement
    }

    @Override
    public void afterCreate() {
        ServerConnector.addTask(((CustomModuleID) getId()).asTaskList(), chatId, result -> {
            if (result.isFailure()) {
                throw new RuntimeException("Critical PinnedMessages initialization error!");
            }
        });
    }

}

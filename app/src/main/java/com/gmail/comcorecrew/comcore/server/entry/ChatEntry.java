package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.ChatID;

public final class ChatEntry {
    public final ChatID id;
    public final String name;

    public ChatEntry(ChatID id, String name) {
        this.id = id;
        this.name = name;
    }
}
package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.classes.AppData;
import com.gmail.comcorecrew.comcore.classes.ReactionHolder;
import com.gmail.comcorecrew.comcore.enums.Reaction;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

import java.util.Map;

/*
 * Class for the cacheable data stored in a messaging module
 */
public class MessageItem implements Cacheable {

    private int userId; //Preferably user id of the message.
    private long messageId; //Message id
    private long timestamp; //Meta data for any use.
    private Reaction myReaction;
    private ReactionHolder reactions;
    private String data; //Data contained in the message.

    public MessageItem(MessageEntry entry) {
        userId = UserStorage.getInternalId(entry.sender);
        messageId = entry.id.id;
        timestamp = entry.timestamp;
        data = entry.contents;
        setReactions(entry.reactions);
    }

    public MessageItem(int userId, long messageId, long timestamp, String data) {
        if (data.length() > (AppData.maxData - 5)) {
            throw new IllegalArgumentException();
        }
        this.userId = userId;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.data = data;
        myReaction = Reaction.NONE;
        reactions = new ReactionHolder();
    }

    public MessageItem(int userId, long messageId, long timestamp, String data,
                       Reaction myReaction, int[] reactions) {
        if (data.length() > (AppData.maxData - 5)) {
            throw new IllegalArgumentException();
        }
        this.userId = userId;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.data = data;
        this.myReaction = myReaction;
        this.reactions = new ReactionHolder(reactions);
    }

    public MessageItem(int userId, long messageId, long timestamp, String data,
                       Reaction myReaction, ReactionHolder reactions) {
        if (data.length() > (AppData.maxData - 5)) {
            throw new IllegalArgumentException();
        }
        this.userId = userId;
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.data = data;
        this.myReaction = myReaction;
        this.reactions = reactions;
    }

    //Creates a new object from cache array.
    public MessageItem(char[] cache) {
        if (cache.length < 14) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        int index = 0;

        //Reads the array into the object.
        userId = cache[index++];
        userId = (userId << 16) | cache[index++];
        messageId = cache[index++];
        messageId = (messageId << 16) | cache[index++];
        messageId = (messageId << 16) | cache[index++];
        messageId = (messageId << 16) | cache[index++];
        timestamp = cache[index++];
        timestamp = (timestamp << 16) | cache[index++];
        timestamp = (timestamp << 16) | cache[index++];
        timestamp = (timestamp << 16) | cache[index++];
        int reaction = cache[index++];
        reaction = (reaction << 16) | cache[index++];
        myReaction = Reaction.fromInt(reaction);
        reactions = new ReactionHolder(cache, index);
        data = new String(cache, index + reactions.getCharLength(), cache.length - (12 + reactions.getCharLength()));
    }

    //Creates a new object from cache string.
    public MessageItem(String global) {
        if (global.length() < 22) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        int index = 24;

        //Reads the array into the object.
        String user = global.substring(0, 24);
        userId = UserStorage.getInternalId(new UserID(user));
        messageId = global.charAt(index++);
        messageId = (messageId << 16) | global.charAt(index++);
        messageId = (messageId << 16) | global.charAt(index++);
        messageId = (messageId << 16) | global.charAt(index++);
        timestamp = global.charAt(index++);
        timestamp = (timestamp << 16) | global.charAt(index++);
        timestamp = (timestamp << 16) | global.charAt(index++);
        timestamp = (timestamp << 16) | global.charAt(index++);
        int reaction = global.charAt(index++);
        reaction = (reaction << 16) | global.charAt(index++);
        myReaction = Reaction.fromInt(reaction);
        reactions = new ReactionHolder(global, index);
        data = global.substring(index + reactions.getCharLength());
    }

    @Override
    public char[] toCache() {
        char[] cache = new char[2 + 4 + 4 + 2 + reactions.getCharLength() +  data.length()];

        int index = 0;

        cache[index++] = (char) (userId >> 16);
        cache[index++] = (char) userId;
        cache[index++] = (char) (messageId >> 48);
        cache[index++] = (char) (messageId >> 32);
        cache[index++] = (char) (messageId >> 16);
        cache[index++] = (char) messageId;
        cache[index++] = (char) (timestamp >> 48);
        cache[index++] = (char) (timestamp >> 32);
        cache[index++] = (char) (timestamp >> 16);
        cache[index++] = (char) timestamp;
        cache[index++] = (char) (myReaction.toInt() >> 16);
        cache[index++] = (char) myReaction.toInt();
        cache[index++] = (char) (reactions.getReactions().length >> 16);
        cache[index++] = (char) reactions.getReactions().length;

        for (int reaction : reactions.getReactions()) {
            cache[index++] = (char) (reaction >> 16);
            cache[index++] = (char) reaction;
        }

        for (int i = 0; i < data.length(); i++) {
            cache[index++] = data.charAt(i);
        }

        return cache;
    }

    public MessageEntry toEntry(ChatID chatID) {
        return new MessageEntry(new MessageID(chatID, messageId),
                UserStorage.getUser(userId).getID(), timestamp, data,
                reactions.toReactionEntries());
    }

    public long getBytes() {
        return 4 + 4 + 8 + 8 + 4 + (reactions.getCharLength() * 2) + (data.length() * 2);
    }

    //Get and Set methods.
    public int getId() {
        return userId;
    }

    public long getMessageid() {
        return messageId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Reaction getMyReaction() {
        return myReaction;
    }

    public ReactionHolder getReactions() {
        return reactions;
    }

    public String getData() {
        return data;
    }

    public void setId(int id) {
        this.userId = id;
    }

    public void setMessageid(long messageid) {
        this.messageId = messageid;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMyReaction(Reaction myReaction) {
        this.myReaction = myReaction;
    }

    public void setReactions(ReactionHolder reactions) {
        this.reactions = reactions;
    }

    public void setReactions(Map<UserID, String> newReactions) {
        myReaction = Reaction.fromString(newReactions.get(AppData.self.getID()));
        reactions = new ReactionHolder();
        for (String reaction : newReactions.values()) {
            reactions.addReaction(Reaction.fromString(reaction));
        }
    }

    public void setData(String data) {
        this.data = data;
    }

    public char[] toGlobal() {
        char[] cache = new char[24 + 4 + 4 + 2 + reactions.getCharLength() +  data.length()];

        int index = 0;

        String user = UserStorage.getUser(userId).getID().id;
        for (int i = 0; i < user.length(); i++) {
            cache[index++] = user.charAt(i);
        }
        cache[index++] = (char) (messageId >> 48);
        cache[index++] = (char) (messageId >> 32);
        cache[index++] = (char) (messageId >> 16);
        cache[index++] = (char) messageId;
        cache[index++] = (char) (timestamp >> 48);
        cache[index++] = (char) (timestamp >> 32);
        cache[index++] = (char) (timestamp >> 16);
        cache[index++] = (char) timestamp;
        cache[index++] = (char) (myReaction.toInt() >> 16);
        cache[index++] = (char) myReaction.toInt();
        cache[index++] = (char) (reactions.getReactions().length >> 16);
        cache[index++] = (char) reactions.getReactions().length;

        for (int reaction : reactions.getReactions()) {
            cache[index++] = (char) (reaction >> 16);
            cache[index++] = (char) reaction;
        }

        for (int i = 0; i < data.length(); i++) {
            cache[index++] = data.charAt(i);
        }

        return cache;
    }
}

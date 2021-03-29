package com.gmail.comcorecrew.comcore.classes.modules;

import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.classes.User;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.MessageID;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;

import java.time.temporal.TemporalAccessor;

public class UserMessage {
    String message;
    User sender;
    String senderName;
    UserInfo userInfo;
    TemporalAccessor time;
    long time2;
    ChatID chatID;
    Group currentGroup;
    MessageID messageID;

    public UserMessage(String message, User sender, TemporalAccessor time, long time2, ChatID chatID, Group currentGroup) {
        super();
        this.message = message;
        this.sender = sender;
        this.userInfo = sender.toUserInfo();
        this.time = time;
        this.time2 = time2;
        this.chatID = chatID;
        this.currentGroup = currentGroup;
    }

    public UserMessage(String message, String senderName, TemporalAccessor time) {
        this.message = message;
        this.senderName = senderName;
        this.time = time;
    }

        public UserMessage(MessageID messageID, UserInfo userInfo, long time, String message) {
        this.messageID = messageID;
        this.userInfo = userInfo;
        this.time2 = time;
        this.message = message;
        this.chatID = messageID.module;
        this.senderName = userInfo.name;
    }

    public TemporalAccessor getTime() {
        return time;
    }


    public MessageID getMessageID() {
        return messageID;
    }

    public String getSenderName() {
        return senderName;
    }


    public UserInfo getUserInfo() {
        return userInfo;
    }

    public long getTime2() {
        return time2;
    }

    public String getMessage() {
        return message;
    }


    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public void setTime(TemporalAccessor time) {
        this.time = time;
    }


    public void setTime2(long time2) {
        this.time2 = time2;
    }

    public void setMessageID(MessageID messageID) {
        this.messageID = messageID;
    }

    public void setUserInfo(UserInfo sender2) {
        this.userInfo = sender2;
    }

    public void setUserName(String sender3) {
        this.senderName = sender3;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ChatID getChatID() {
        return chatID;
    }

    public Group getCurrentGroup() {
        return currentGroup;
    }

    public void setChatID(ChatID chatID) {
        this.chatID = chatID;
    }

    public void setCurrentGroup(Group currentGroup) {
        this.currentGroup = currentGroup;
    }
}

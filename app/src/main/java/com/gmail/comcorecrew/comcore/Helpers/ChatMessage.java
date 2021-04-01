package com.gmail.comcorecrew.comcore.Helpers;

public class ChatMessage {
    public boolean left;
    public String message;
    public long time;

    public ChatMessage(boolean left, String message, long time) {
        super();
        this.left = left;
        this.message = message;
        this.time = time;
    }
}

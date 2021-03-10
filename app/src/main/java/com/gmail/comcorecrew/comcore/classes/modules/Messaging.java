package com.gmail.comcorecrew.comcore.classes.modules;

import android.content.Context;

import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.caching.StdCacheable;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.interfaces.Module;

import java.util.ArrayList;

public class Messaging implements Module {

    private String name; //Name of chat
    private final Group group; //Group that the chat is in
    private final int mnum; //Module number
    private ArrayList<StdCacheable> messages; //Messages

    public Messaging(Group group, String name) {
        this.group = group;
        this.name = name;
        this.mnum = group.addModule(this);
        messages = new ArrayList<>();
    }

    @Override
    public String getMdid() {
        return "cmsg";
    }

    @Override
    public int getGroupId() {
        return group.getGroupId();
    }

    public int getMnum() {
        return mnum;
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
    public boolean toCache(Context context) {
        if (messages.size() == 0) {
            return true;
        }

        int index = messages.size() - 1;
        long bytes = messages.get(index).getBytes();
        long byteGoal = 1000;
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
            messages.add(new StdCacheable(line));
        }
        return true;
    }
}

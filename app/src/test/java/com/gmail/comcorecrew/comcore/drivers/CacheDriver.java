package com.gmail.comcorecrew.comcore.drivers;

import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.enums.GroupRole;
import com.gmail.comcorecrew.comcore.interfaces.Module;
import com.gmail.comcorecrew.comcore.server.NotificationListener;
import com.gmail.comcorecrew.comcore.server.entry.GroupInviteEntry;
import com.gmail.comcorecrew.comcore.server.entry.MessageEntry;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.ArrayList;
import java.util.Collection;

public class CacheDriver implements Module {

    private ArrayList<Cacheable> data = null;

    @Override
    public String getMdid() {
        return "test";
    }

    @Override
    public int getMnum() {
        return 0;
    }

    @Override
    public boolean toCache() {
        return Cacher.cacheData(data, this);
    }

    @Override
    public boolean fromCache() {
        char[][] rawData = Cacher.uncacheData(this);
        if (rawData == null) {
            return false;
        }
        data = new ArrayList<>();
        for (char[] line : rawData) {
            data.add(new CacheableDriver(String.copyValueOf(line)));
        }
        return true;
    }

    @Override
    public String getGroupId() {
        return "0";
    }

    public ArrayList<Cacheable> getData() {
        return data;
    }

    public void setData(ArrayList<Cacheable> data) {
        this.data = data;
    }

    @Override
    public void onReceiveMessage(MessageEntry message) {}

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

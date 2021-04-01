package com.gmail.comcorecrew.comcore.drivers;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.server.id.ChatID;
import com.gmail.comcorecrew.comcore.server.id.GroupID;

import java.util.ArrayList;

public class CacheDriver extends Module {

    private ArrayList<Cacheable> data = null;

    public CacheDriver() {
        super("TEST", new ChatID(new GroupID("grTEST"), "testID"),
                new Group(new GroupID("grTEST")), Mdid.TEST);
    }

    public int getMnum() {
        return 0;
    }

    public void setData(ArrayList<Cacheable> data) {
        this.data = data;
    }

    public ArrayList<Cacheable> getData() {
        return data;
    }

    @Override
    public void readToCache() {
        Cacher.cacheData(data, this);
    }

    @Override
    public void readFromCache() {
        char[][] rawData = Cacher.uncacheData(this);
        if (rawData == null) {
            return;
        }
        data = new ArrayList<>();
        for (char[] line : rawData) {
            data.add(new CacheableDriver(String.copyValueOf(line)));
        }
    }

    @Override
    public void setMuted(boolean muted) {

    }

    @Override
    public boolean isMuted() {
        return false;
    }
}

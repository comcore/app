package com.gmail.comcorecrew.comcore.drivers;

import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.interfaces.Module;

import java.io.File;
import java.util.ArrayList;

public class CacheDriver implements Module {

    private ArrayList<Cacheable> data = null;

    @Override
    public Mdid getMdid() {
        return Mdid.TEST;
    }

    @Override
    public int getMnum() {
        return 0;
    }

    @Override
    public void toCache() {
        Cacher.cacheData(data, this);
    }

    @Override
    public void fromCache() {
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
    public void toFile() {

    }

    @Override
    public void fromFile(File file, Group group) {

    }

    @Override
    public String getGroupId() {
        return "0";
    }

    @Override
    public void setMuted(boolean muted) {

    }

    @Override
    public boolean getMuted() {
        return false;
    }

    public ArrayList<Cacheable> getData() {
        return data;
    }

    public void setData(ArrayList<Cacheable> data) {
        this.data = data;
    }
}

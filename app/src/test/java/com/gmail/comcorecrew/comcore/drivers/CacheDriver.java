package com.gmail.comcorecrew.comcore.drivers;

import android.content.Context;

import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.interfaces.Module;

import java.util.ArrayList;

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
    public boolean toCache(Context context) {
        return Cacher.cacheData(data, this, context);
    }

    @Override
    public boolean fromCache(Context context) {
        char[][] rawData = Cacher.uncacheData(this, context);
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
    public int getGroupId() {
        return 0;
    }

    public ArrayList<Cacheable> getData() {
        return data;
    }

    public void setData(ArrayList<Cacheable> data) {
        this.data = data;
    }
}

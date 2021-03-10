package com.gmail.comcorecrew.comcore.drivers;

import com.gmail.comcorecrew.comcore.caching.Cacheable;

public class CacheableDriver implements Cacheable {

    private String data;

    public CacheableDriver(String data) {
        this.data = data;
    }
    @Override
    public char[] toCache() {
        char[] cacheData = new char[data.length()];

        for (int i = 0; i < data.length(); i++) {
            cacheData[i] = data.charAt(i);
        }

        return cacheData;
    }

    public String getData() {
        return data;
    }
}

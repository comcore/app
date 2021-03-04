package com.gmail.comcorecrew.comcore.classes;

import com.gmail.comcorecrew.comcore.interfaces.Cacheable;

/*
 * Class for the standard cacheable format. Supports the caching of an int,
 * a long, and a string up to 4 MB long.
 */
public class StdCacheable implements Cacheable {

    private int id; //Preferably user id of the message.
    private long meta; //Meta data for any use.
    private String data; //Data contained in the message.

    public StdCacheable(int id, long meta, String data) {
        if (data.length() > 0x001E8483) { //4 MB of data + 6 bytes
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.meta = meta;
        this.data = data;
    }

    //Creates a new object from cache array.
    public StdCacheable(char[] cache) {
        if (cache.length < 6) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        //Reads the array into the object.
        id = cache[0];
        id = (id << 16) | cache[1];
        meta = cache[2];
        meta = (meta << 16) | cache[3];
        meta = (meta << 16) | cache[4];
        meta = (meta << 16) | cache[5];
        data = new String(cache, 6, cache.length - 6);
    }

    @Override
    public char[] toCache() {
        char[] cache = new char[2 + 4 + data.length()];

        cache[0] = (char) (id >> 16);
        cache[1] = (char) id;
        cache[2] = (char) (meta >> 48);
        cache[3] = (char) (meta >> 32);
        cache[4] = (char) (meta >> 16);
        cache[5] = (char) meta;
        for (int i = 0; i < data.length(); i++) {
            cache[6 + i] = data.charAt(i);
        }

        return cache;
    }

    //Reads a char array from cache into the data.
    public void fromCache(char[] cache) {
        if (cache.length < 6) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        //Reads the array into the object.
        id = cache[0];
        id = (id << 16) | cache[1];
        meta = cache[2];
        meta = (meta << 16) | cache[3];
        meta = (meta << 16) | cache[4];
        meta = (meta << 16) | cache[5];
        data = new String(cache, 6, cache.length - 6);
    }

    //Get and Set methods.
    public int getId() {
        return id;
    }

    public long getMeta() {
        return meta;
    }

    public String getData() {
        return data;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMeta(long meta) {
        this.meta = meta;
    }

    public void setData(String data) {
        this.data = data;
    }
}

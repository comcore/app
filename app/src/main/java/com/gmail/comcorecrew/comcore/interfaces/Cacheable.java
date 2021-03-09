package com.gmail.comcorecrew.comcore.interfaces;

/*
 * Interface for creating data holding objects that can be cached.
 */
public interface Cacheable {

    // Returns the data that is to be cached.
    char[] toCache();

}

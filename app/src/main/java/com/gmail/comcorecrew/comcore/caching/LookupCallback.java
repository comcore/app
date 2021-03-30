package com.gmail.comcorecrew.comcore.caching;

/**
 * A callback that can be used to do something after looking up a User or Group.
 *
 * @param <T> the argument type
 */
public interface LookupCallback<T> {
    /**
     * Do something with the given argument.
     *
     * @param info the argument
     */
    void accept(T info);
}
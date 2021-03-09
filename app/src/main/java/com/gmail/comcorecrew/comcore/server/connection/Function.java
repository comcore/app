package com.gmail.comcorecrew.comcore.server.connection;

/**
 * Represents a transformation which can be applied to some data.
 *
 * @param <T> the original type
 * @param <U> the new type after the transformation
 */
public interface Function<T, U> {
    /**
     * Apply a transformation to some data.
     *
     * @param data the input data
     * @return the output data
     */
    U apply(T data);
}
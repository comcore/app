package com.gmail.comcorecrew.comcore.exceptions;

/**
 * Class used to store exception that is thrown when the user storage
 * files are disjointed from each other.
 */
public class StorageFileDisjunctionException extends RuntimeException {
    public StorageFileDisjunctionException() {
        super();
    }

    public StorageFileDisjunctionException(String message) {
        super(message);
    }

    public StorageFileDisjunctionException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageFileDisjunctionException(Throwable cause) {
        super(cause);
    }
}

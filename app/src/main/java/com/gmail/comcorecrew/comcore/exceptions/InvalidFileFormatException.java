package com.gmail.comcorecrew.comcore.exceptions;

import java.io.IOException;

/**
 * Exception thrown when file format is corrupted.
 */
public class InvalidFileFormatException extends IOException {
    public InvalidFileFormatException() {
        super();
    }

    public InvalidFileFormatException(String message) {
        super(message);
    }

    public InvalidFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFileFormatException(Throwable cause) {
        super(cause);
    }
}

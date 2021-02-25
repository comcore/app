package com.gmail.comcorecrew.comcore.server;

/**
 * A ServerConnector which always returns the same result for testing purposes.
 */
public class MockConnector extends ServerConnector {
    private final ServerResult<String> result;

    /**
     * Creates a mock ServerConnector given a value to always return.
     *
     * @param result the result which will always be returned
     */
    public MockConnector(ServerResult<String> result) {
        this.result = result;
    }

    @Override
    protected ServerResult<String> send(String message) {
        return result;
    }
}
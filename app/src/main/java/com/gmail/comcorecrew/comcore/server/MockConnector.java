package com.gmail.comcorecrew.comcore.server;

import com.google.gson.JsonObject;

/**
 * A ServerConnector which always returns the same result for testing purposes.
 */
public class MockConnector extends ServerConnector {
    private final ServerResult<JsonObject> result;

    /**
     * Creates a mock ServerConnector given a value to always return.
     *
     * @param result the result which will always be returned
     */
    public MockConnector(ServerResult<JsonObject> result) {
        this.result = result;
    }

    @Override
    protected ServerResult<JsonObject> sendSync(String kind, JsonObject message) {
        return result;
    }
}
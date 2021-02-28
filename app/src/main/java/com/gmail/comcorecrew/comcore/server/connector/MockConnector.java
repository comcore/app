package com.gmail.comcorecrew.comcore.server.connector;

import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.google.gson.JsonObject;

/**
 * A ServerConnector which always returns the same result for testing purposes.
 */
public final class MockConnector extends ServerConnector {
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
    protected void authenticate(String email, String pass, ResultHandler<Void> handler) {
        if (handler != null) {
            handler.handleResult(ServerResult.success(null));
        }
    }

    @Override
    protected <T> void send(String kind, JsonObject data, ResultHandler<T> handler,
                            ServerResult.Function<JsonObject, T> function) {
        if (handler != null) {
            handler.handleResult(result.tryMap(function));
        }
    }
}
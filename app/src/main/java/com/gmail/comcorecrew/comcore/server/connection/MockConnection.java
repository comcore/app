package com.gmail.comcorecrew.comcore.server.connection;

import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.gmail.comcorecrew.comcore.server.LoginStatus;
import com.google.gson.JsonObject;

/**
 * A ServerConnector which always returns the same result for testing purposes.
 */
public final class MockConnection implements Connection {
    private final ServerResult<JsonObject> result;

    /**
     * Creates a mock ServerConnector given a value to always return.
     *
     * @param result the result which will always be returned
     */
    public MockConnection(ServerResult<JsonObject> result) {
        this.result = result;
    }

    @Override
    public void stop() {}

    @Override
    public void logout() {}

    @Override
    public void authenticate(String email, String pass, boolean createAccount,
                             ResultHandler<LoginStatus> handler) {
        if (handler != null) {
            handler.handleResult(ServerResult.success(LoginStatus.SUCCESS));
        }
    }

    @Override
    public <T> void send(Message message, ResultHandler<T> handler,
                         Function<JsonObject, T> function) {
        if (handler != null) {
            handler.handleResult(result.map(function));
        }
    }
}
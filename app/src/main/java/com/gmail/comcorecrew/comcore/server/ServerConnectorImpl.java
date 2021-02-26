package com.gmail.comcorecrew.comcore.server;

import com.google.gson.JsonObject;

/**
 * Default implementation of the abstract ServerConnector class.
 */
public class ServerConnectorImpl extends ServerConnector {
    // TODO implement server connection

    @Override
    protected ServerResult<JsonObject> sendSync(String kind, JsonObject message) {
        return ServerResult.failure("unimplemented");
    }
}
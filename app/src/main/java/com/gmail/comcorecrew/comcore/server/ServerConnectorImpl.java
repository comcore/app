package com.gmail.comcorecrew.comcore.server;

/**
 * Default implementation of the abstract ServerConnector class.
 */
public class ServerConnectorImpl extends ServerConnector {
    // TODO implement server connection

    @Override
    protected ServerResult<String> send(String message) {
        return ServerResult.failure("unimplemented");
    }
}
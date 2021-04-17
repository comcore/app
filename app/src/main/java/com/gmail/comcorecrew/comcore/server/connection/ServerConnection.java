package com.gmail.comcorecrew.comcore.server.connection;

import com.gmail.comcorecrew.comcore.notifications.NotificationListener;
import com.gmail.comcorecrew.comcore.server.LoginToken;
import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.gmail.comcorecrew.comcore.server.connection.thread.ServerReader;
import com.gmail.comcorecrew.comcore.server.connection.thread.ServerWriter;
import com.gmail.comcorecrew.comcore.server.LoginStatus;
import com.gmail.comcorecrew.comcore.server.info.UserInfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;

/**
 * Default implementation of the abstract ServerConnector class.
 */
public final class ServerConnection implements Connection {
    private static final String SERVER_URL = "comcore.ml";
    private static final int SERVER_PORT = 4433;
    private static final boolean DEBUG = false;

    private final String url;

    private SSLContext sslContext;
    private ServerWriter writerThread;
    private ServerReader readerThread;

    private String email;
    private String pass;
    private LoginToken token;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Create a new ServerConnection.
     */
    public ServerConnection() {
        this(SERVER_URL);
    }

    /**
     * Create a new ServerConnection at a given URL.
     */
    public ServerConnection(String url) {
        this.url = url;

        try {
            // Initialize the SSLContext
            sslContext = SSLContext.getDefault();

            // Start threads for writing and reading
            writerThread = new ServerWriter(this);
            readerThread = new ServerReader(this);
        } catch (Exception e) {
            // If there was an exception during initialization, clean up
            stop();
            e.printStackTrace();
        }
    }

    /**
     * Connect to the server if not already connected.
     *
     * @return true if connected to the server, false otherwise
     */
    private synchronized boolean start() {
        // If there isn't an SSLContext, don't try to start the connection
        if (sslContext == null) {
            return false;
        }

        // If already connected, nothing needs to be done
        if (socket != null && !socket.isClosed()) {
            return true;
        }

        if (DEBUG) {
            System.err.println("ServerConnection.start()");
        }

        try {
            // Create a Socket connected to the server
            InetSocketAddress endPoint = new InetSocketAddress(url, SERVER_PORT);
            socket = new Socket();
            socket.connect(endPoint, 5_000);

            // Add SSL to the socket
            socket = sslContext.getSocketFactory()
                    .createSocket(socket, url, SERVER_PORT, true);

            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            // If there was an error connecting, clean up and fail
            close();
            return false;
        }

        // Resend login details if possible
        authenticate();

        // Wake up any waiting threads
        notifyAll();

        return true;
    }

    /**
     * Close the connection to the server and immediately fail any pending tasks.
     */
    private synchronized void close() {
        // If the connection is already closed, nothing needs to be done
        if (socket == null) {
            return;
        }

        if (DEBUG) {
            System.err.println("ServerConnection.close()");
        }

        // Close the socket, ignoring any errors
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear the variables related to the socket
        socket = null;
        in = null;
        out = null;

        // Clear the tasks from the queues
        readerThread.clearTasks();
        writerThread.clearTasks();
    }

    /**
     * Add a task to execute on the writer thread.
     *
     * @param task the task to execute
     */
    private synchronized void addTask(ServerTask task) {
        writerThread.addTask(task);
    }

    /**
     * Resend login information when a connection is restarted but the user already logged in.
     */
    private void authenticate() {
        String email;
        String pass;

        synchronized (this) {
            // Try connecting with a token if there is one
            if (token != null) {
                connect(token);
                return;
            }

            email = this.email;
            pass = this.pass;
        }

        if (email == null) {
            return;
        }

        JsonObject data = new JsonObject();
        data.addProperty("email", email);
        if (pass != null) {
            data.addProperty("pass", pass);
        }
        startTask(new ServerTask(new ServerMsg(pass == null ? "requestReset" : "login", data), result -> {
            if (result.isFailure()) {
                // A failure doesn't necessarily mean the information was wrong
                return;
            }

            try {
                if (pass == null) {
                    if (result.data.get("sent").getAsBoolean()) {
                        // The password reset was continued, so do nothing
                        return;
                    }
                } else if (LoginStatus.fromJson(result.data) == LoginStatus.SUCCESS) {
                    // The login was successful, so do nothing
                    return;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            // The login failed, so log the user out so they can enter new information
            loggedOut();
        }));
    }

    /**
     * Record that the user was logged out from the server, sending a notification if the user
     * wasn't already logged out.
     */
    public void loggedOut() {
        if (token != null) {
            ServerConnector.sendNotification(NotificationListener::onLoggedOut);
        }

        setInformation(null, null);
    }

    /**
     * Record that the user was logged into the server with the given information, sending a
     * notification if the user wasn't already logged in or was logged into a different account.
     *
     * @param userInfo the information of the user
     * @param token    the login token of the user
     */
    public void loggedIn(UserInfo userInfo, LoginToken token) {
        if (token == this.token) {
            return;
        }

        this.token = token;
        ServerConnector.sendNotification(listener ->
                listener.onLoggedIn(userInfo, token));
    }

    /**
     * Start a task on the current thread and add it to the reader thread pending a response. This
     * should only be called from the WriterThread as it is an error to connect a socket from the
     * main thread.
     *
     * @param task the task to start
     */
    public void startTask(ServerTask task) {
        if (task == null) {
            return;
        }

        // If the message is a ping and there isn't anything queued, then just call it directly
        if (task.message.isPing() && readerThread.isEmpty()) {
            task.handleResult(ServerResult.success(new JsonObject()));
            return;
        }

        if (!start()) {
            task.handleResult(ServerResult.failure("cannot connect to server"));
            return;
        }

        if (DEBUG) {
            System.err.println("--> " + task.message.toJson());
        }

        out.println(task.message.toJson());

        if (out.checkError()) {
            close();
            connect();
            task.handleResult(ServerResult.failure("failed to send message"));
            return;
        }

        readerThread.addTask(task);
    }

    /**
     * Try to connect to the server if not already connected. This should only be called from the
     * WriterThread, as it is an error to try to connect a socket from the main thread.
     */
    public void connect() {
        start();
    }

    /**
     * Receive a message from the server on the current thread. This should only be called from the
     * ReaderThread, as doing otherwise might lead to a message reply being dropped.
     *
     * @return the next message from the server or null on failure
     */
    public ServerMsg receiveMessage() {
        synchronized (this) {
            while (sslContext != null && in == null) {
                try {
                    wait(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (sslContext == null) {
                return null;
            }
        }

        try {
            String line = in.readLine();
            if (line != null && !line.isEmpty()) {
                if (DEBUG) {
                    System.err.println("<-- " + line);
                }

                JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                return ServerMsg.fromJson(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // There was no valid message sent, which is an invalid state so restart the connection
        close();
        connect();
        return null;
    }

    @Override
    public void stop() {
        synchronized (this) {
            setInformation(null, null);
            sslContext = null;

            close();

            notifyAll();
        }

        if (writerThread != null) {
            writerThread.join();
        }

        if (readerThread != null) {
            readerThread.join();
        }
    }

    @Override
    public void logout()  {
        loggedOut();

        addTask(new ServerTask(new ServerMsg("logout"), null));
    }

    @Override
    public synchronized void setInformation(String email, String pass) {
        if (pass == null) {
            token = null;
        }

        if (email != null || pass == null) {
            this.email = email;
        }

        this.pass = pass;
    }

    @Override
    public synchronized void connect(LoginToken token) {
        this.token = token;

        JsonObject data = new JsonObject();
        data.addProperty("id", token.user.id);
        data.addProperty("token", token.token);
        addTask(new ServerTask(new ServerMsg("connect", data), null));
    }

    @Override
    public <T> void send(ServerMsg message, ResultHandler<T> handler,
                         Function<JsonObject, T> function) {
        addTask(new ServerTask(message, handler == null ? null : result ->
                handler.handleResult(result.map(function))));
    }
}
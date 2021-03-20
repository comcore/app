package com.gmail.comcorecrew.comcore.server.connection;

import android.content.Context;

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
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Default implementation of the abstract ServerConnector class.
 */
public final class ServerConnection implements Connection {
    private static final String SERVER_URL = "ec2-18-188-151-48.us-east-2.compute.amazonaws.com";
    private static final int SERVER_PORT = 4433;

    private final String url;

    private SSLContext sslContext;
    private ServerWriter writerThread;
    private ServerReader readerThread;

    private String email;
    private String pass;
    private UserInfo userInfo;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Create a new ServerConnector in the given context.
     *
     * @param context the application context to create the ServerConnector in
     */
    public ServerConnection(Context context) {
        this(context, SERVER_URL);
    }

    /**
     * Create a new ServerConnector in the given context attached to a certain URL.
     *
     * @param context the application context to create the ServerConnector in
     * @param url     the URL to connect to
     */
    public ServerConnection(Context context, String url) {
        this.url = url;

        try {
            // Load the server's certificate from the assets folder
            Certificate certificate = CertificateFactory.getInstance("X.509")
                    .generateCertificate(context.getAssets().open("cert.pem"));

            // Create a KeyStore containing the certificate
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("server", certificate);

            // Use the KeyStore to create a TrustManagerFactory
            String defaultAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tfm = TrustManagerFactory.getInstance(defaultAlgorithm);
            tfm.init(keyStore);

            // Initialize the SSLContext with the TrustManagerFactory
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tfm.getTrustManagers(), null);

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
    private synchronized void addTask(Task task) {
        writerThread.addTask(task);
    }

    /**
     * Resend login information when a connection is restarted but the user already logged in.
     */
    private void authenticate() {
        String email;
        String pass;

        synchronized (this) {
            email = this.email;
            pass = this.pass;
        }

        if (email == null) {
            return;
        }

        if (pass == null) {
            // They were trying to do a password reset and got disconnected

            loggedOut();
            return;
        }

        JsonObject data = new JsonObject();
        data.addProperty("email", email);
        data.addProperty("pass", pass);
        startTask(new Task(new Message("login", data), result -> {
            if (result.isFailure()) {
                // A failure doesn't necessarily mean the information was wrong
                return;
            }

            try {
                if (LoginStatus.fromJson(result.data) == LoginStatus.SUCCESS) {
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
     * Record that the user was logged out from the server and that their email and password must
     * be entered again to continue. This could happen if their password was changed or if the
     * client wasn't able to log in after being automatically reconnected.
     */
    public void loggedOut() {
        setInformation(null, null);

        ServerConnector.foreachListener(listener -> {
            listener.onLoggedOut();
            return false;
        });
    }

    /**
     * Set the information of the user when the server sends it.
     *
     * @param userData the user data
     */
    public synchronized void setUserInfo(UserInfo userData) {
        this.userInfo = userData;
    }

    /**
     * Start a task on the current thread and add it to the reader thread pending a response. This
     * should only be called from the WriterThread as it is an error to connect a socket from the
     * main thread.
     *
     * @param task the task to start
     */
    public void startTask(Task task) {
        if (task == null) {
            return;
        }

        if (!start()) {
            task.handleResult(ServerResult.failure("cannot connect to server"));
            return;
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
    public Message receiveMessage() {
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
                JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                return Message.fromJson(json);
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
        setInformation(null, null);

        addTask(new Task(new Message("logout"), null));
    }

    @Override
    public synchronized UserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public synchronized void setInformation(String email, String pass) {
        if (pass == null) {
            this.userInfo = null;
        }

        if (email != null || pass == null) {
            this.email = email;
        }

        this.pass = pass;
    }

    @Override
    public <T> void send(Message message, ResultHandler<T> handler,
                         Function<JsonObject, T> function) {
        addTask(new Task(message, handler == null ? null : result ->
                handler.handleResult(result.map(function))));
    }
}
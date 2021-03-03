package com.gmail.comcorecrew.comcore.server.connection;

import android.content.Context;

import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.connection.thread.ServerReader;
import com.gmail.comcorecrew.comcore.server.connection.thread.ServerWriter;
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
    private static final int SERVER_PORT = 443;

    private SSLContext sslContext;
    private ServerWriter writerThread;
    private ServerReader readerThread;

    private String email;
    private String pass;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Create a new ServerConnector in the given context.
     *
     * @param context the application context to create the ServerConnector in
     */
    public ServerConnection(Context context) {
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
            InetSocketAddress endPoint = new InetSocketAddress(SERVER_URL, SERVER_PORT);
            socket = new Socket();
            socket.connect(endPoint, 5_000);

            // Add SSL to the socket
            socket = sslContext.getSocketFactory()
                    .createSocket(socket, SERVER_URL, SERVER_PORT, true);

            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            // If there was an error connecting, clean up and fail
            e.printStackTrace();
            close();
            return false;
        }

        // Resend login details if possible
        reauthenticate();

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
    private void reauthenticate() {
        String email;
        String pass;

        synchronized (this) {
            email = this.email;
            pass = this.pass;
        }

        if (email == null || pass == null) {
            return;
        }

        JsonObject message = new JsonObject();
        message.addProperty("email", email);
        message.addProperty("pass", pass);
        startTask(new Task(new Message("login", message), null));
    }

    /**
     * Start a task on the current thread and add it to the reader thread pending a response.
     *
     * @param task the task to start
     */
    public void startTask(Task task) {
        if (task == null) {
            return;
        }

        if (!start()) {
            task.fail("cannot connect to server");
            return;
        }

        out.println(task.message.toJson());

        if (out.checkError()) {
            close();
            task.fail("failed to send message");
            return;
        }

        readerThread.addTask(task);
    }

    /**
     * Receive a message from the server on the current thread.
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
            if (line == null) {
                // The server finished sending data, so close the connection
                close();
                return null;
            }

            JsonObject json = JsonParser.parseString(line).getAsJsonObject();
            return Message.fromJson(json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void stop() {
        synchronized (this) {
            close();

            email = null;
            pass = null;
            sslContext = null;

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
    public void authenticate(String email, String pass, ResultHandler<Void> handler) {
        if (email == null || pass == null) {
            throw new IllegalArgumentException("email address and password cannot be null");
        }

        synchronized (this) {
            this.email = null;
            this.pass = null;
        }

        JsonObject data = new JsonObject();
        data.addProperty("email", email);
        data.addProperty("pass", pass);
        addTask(new Task(new Message("login", data), result -> {
            if (result.isSuccess()) {
                synchronized (this) {
                    this.email = email;
                    this.pass = pass;
                }
            }
            if (handler != null) {
                handler.handleResult(result.map(response -> null));
            }
        }));
    }

    @Override
    public <T> void send(Message message, ResultHandler<T> handler,
                         Function<JsonObject, T> function) {
        addTask(new Task(message, handler == null ? null : result -> {
            handler.handleResult(result.tryMap(function));
        }));
    }
}
package com.gmail.comcorecrew.comcore.server.connector;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.gmail.comcorecrew.comcore.server.ResultHandler;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.ServerResult;
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
import java.util.ArrayDeque;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Default implementation of the abstract ServerConnector class.
 */
public final class ServerConnectorImpl extends ServerConnector {
    private static final String SERVER_URL = "ec2-18-188-151-48.us-east-2.compute.amazonaws.com";
    private static final int SERVER_PORT = 443;

    private static class Task {
        final String kind;
        final JsonObject data;
        final ResultHandler<JsonObject> handler;

        private Task(String kind, JsonObject data, ResultHandler<JsonObject> handler) {
            this.kind = kind;
            this.data = data;
            this.handler = handler;
        }
    }

    private ArrayDeque<Task> taskQueue = new ArrayDeque<>();

    private SSLContext sslContext;
    private Thread thread;

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
    public ServerConnectorImpl(Context context) {
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

            // Create a new thread to connect to the server
            thread = new Thread(() -> {
                // Run while the server is initialized
                while (sslContext != null) {
                    Task task;
                    synchronized (this) {
                        // Wait until there is a task in the queue
                        while (taskQueue.isEmpty()) {
                            try {
                                this.wait(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        // Remove a task from the queue
                        task = taskQueue.pop();
                    }

                    // Execute the task
                    runTask(task);
                }
            }, "ServerConnection");
            thread.start();
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
    private boolean start() {
        // If there isn't an SSLContext, don't try to start the connection
        if (sslContext == null) {
            return false;
        }

        // If already connected, nothing needs to be done
        if (socket != null && socket.isConnected()) {
            return true;
        }

        try {
            // Create a Socket connected to the server
            InetSocketAddress endPoint = new InetSocketAddress(SERVER_URL, SERVER_PORT);
            Socket socket = new Socket();
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
        return true;
    }

    /**
     * Close the connection to the server and immediately fail any pending tasks.
     */
    private void close() {
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

        // Replace the task queue with an empty queue
        ArrayDeque<Task> tasks;
        synchronized (this){
            if (taskQueue.isEmpty()) {
                // The task queue is empty, so no cleanup needs to be done
                return;
            }

            tasks = taskQueue;
            taskQueue = new ArrayDeque<>();
        }

        // On the main thread, fail all of the pending tasks
        new Handler(Looper.getMainLooper()).post(() -> {
            for (Task task : tasks) {
                if (task.handler != null) {
                    task.handler.handleResult(ServerResult.failure("disconnected from server"));
                }
            }
        });
    }

    private ServerResult<JsonObject> sendSync(String kind, JsonObject message) {
        if (!start()) {
            return ServerResult.failure("cannot connect to server");
        }

        out.println(message.toString());
        out.flush();
        if (out.checkError()) {
            close();
            return ServerResult.failure("failed to send message");
        }

        try {
            JsonObject response = JsonParser.parseString(in.readLine()).getAsJsonObject();
            if (response.get("status").getAsString().equals("OK")) {
                return ServerResult.success(response.get("contents").getAsJsonObject());
            } else {
                return ServerResult.failure(response.get("message").getAsString());
            }
        } catch (Exception e) {
            return ServerResult.invalidResponse();
        }
    }

    private void runTask(Task task) {
        if (task == null) {
            return;
        }

        ServerResult<JsonObject> result = sendSync(task.kind, task.data);
        if (task.handler != null) {
            new Handler(Looper.getMainLooper()).post(() -> task.handler.handleResult(result));
        }
    }

    private synchronized void addTask(Task task) {
        if (task == null) {
            return;
        }

        taskQueue.add(task);
        this.notifyAll();
    }

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
        runTask(new Task("login", message, null));
    }

    @Override
    protected void stop() {
        close();

        email = null;
        pass = null;
        sslContext = null;

        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void authenticate(String email, String pass, ResultHandler<Void> handler) {
        if (email == null || pass == null) {
            throw new IllegalArgumentException("email address and password cannot be null");
        }

        synchronized (this) {
            this.email = null;
            this.pass = null;
        }

        JsonObject message = new JsonObject();
        message.addProperty("email", email);
        message.addProperty("pass", pass);
        addTask(new Task("login", message, result -> {
            if (result.isSuccess()) {
                synchronized (this) {
                    this.email = email;
                    this.pass = pass;
                }
            }
            if (handler != null) {
                handler.handleResult(result.tryMap(response -> null));
            }
        }));
    }

    @Override
    protected <T> void send(String kind, JsonObject data, ResultHandler<T> handler,
                            ServerResult.Function<JsonObject, T> function) {
        addTask(new Task(kind, data, handler == null ? null : result -> {
            handler.handleResult(result.tryMap(function));
        }));
    }
}
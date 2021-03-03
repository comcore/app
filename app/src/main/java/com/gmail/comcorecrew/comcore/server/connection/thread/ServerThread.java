package com.gmail.comcorecrew.comcore.server.connection.thread;

import com.gmail.comcorecrew.comcore.server.ServerResult;
import com.gmail.comcorecrew.comcore.server.connection.ServerConnection;
import com.gmail.comcorecrew.comcore.server.connection.Task;

import java.util.ArrayDeque;

/**
 * Represents a thread associated with a ServerConnection which contains a queue of tasks.
 */
public abstract class ServerThread {
    private final Thread thread;
    private ArrayDeque<Task> currentTasks = new ArrayDeque<>();
    private boolean running = true;

    /**
     * The associated ServerConnection instance.
     */
    protected ServerConnection connection;

    /**
     * Create a ServerThread associated with a ServerConnection.
     *
     * @param connection the ServerConnection which will handle the tasks
     */
    public ServerThread(ServerConnection connection) {
        this.connection = connection;
        thread = new Thread(() -> {
            while (running) {
                step();
            }
        }, getClass().getSimpleName());
        thread.start();
    }

    /**
     * Handle tasks in a loop while the ServerConnection is running.
     */
    protected abstract void step();

    /**
     * Add a task to the queue.
     *
     * @param task the task to add
     */
    public final synchronized void addTask(Task task) {
        if (running) {
            currentTasks.add(task);
            notifyAll();
        } else {
            task.handleResult(ServerResult.disconnected());
        }
    }

    /**
     * Get a task from the queue.
     *
     * @return the next task from the queue
     */
    protected final synchronized Task getTask() {
        while (running && currentTasks.isEmpty()) {
            try {
                wait(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!running) {
            return null;
        }

        return currentTasks.pop();
    }

    /**
     * Clear all tasks from the queue, failing them.
     */
    public final synchronized void clearTasks() {
        for (Task task : currentTasks) {
            task.handleResult(ServerResult.disconnected());
        }
        currentTasks.clear();
    }

    /**
     * Stop running this thread and join it.
     */
    public final void join() {
        synchronized (this) {
            running = false;
            notifyAll();
        }

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the thread is still running.
     *
     * @return true if the thread is running, false otherwise
     */
    public final boolean isRunning() {
        return running;
    }
}
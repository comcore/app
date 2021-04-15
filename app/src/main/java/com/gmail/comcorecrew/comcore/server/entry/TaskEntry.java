package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an entry of task data returned by the server.
 */
public final class TaskEntry {
    /**
     * The task's identifier.
     */
    public final TaskID id;

    /**
     * The user that created the task.
     */
    public final UserID creator;

    /**
     * The UNIX timestamp representing when the task was added.
     */
    public final long timestamp;

    /**
     * The description of the task.
     */
    public final String description;

    /**
     * Whether the task has been marked as completed.
     */
    public final boolean completed;

    /**
     * Create a TaskEntry with an owner, a description of the task, and whether it is completed.
     *
     * @param id          the TaskID of the task
     * @param creator     the user that created the task
     * @param description the description of the task
     * @param completed   whether the task has been completed
     */
    public TaskEntry(TaskID id, UserID creator, long timestamp, String description,
                     boolean completed) {
        if (id == null) {
            throw new IllegalArgumentException("TaskID cannot be null");
        } else if (creator == null) {
            throw new IllegalArgumentException("task creator cannot be null");
        } else if (timestamp < 1) {
            throw new IllegalArgumentException("task timestamp cannot be less than 1");
        } else if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("task description cannot be null or empty");
        }

        this.id = id;
        this.creator = creator;
        this.timestamp = timestamp;
        this.description = description;
        this.completed = completed;
    }

    /**
     * Create a TaskEntry from a JsonObject. If specified, the given TaskListID is used. Otherwise,
     * it is taken from the message data.
     *
     * @param taskList the task list which contains the task or null
     * @param json     the data sent by the server
     * @return the TaskEntry
     */
    public static TaskEntry fromJson(TaskListID taskList, JsonObject json) {
        TaskID id = TaskID.fromJson(taskList, json);
        UserID creator = new UserID(json.get("owner").getAsString());
        long timestamp = json.get("timestamp").getAsLong();
        String description = json.get("description").getAsString();
        boolean completed = json.get("completed").getAsBoolean();
        return new TaskEntry(id, creator, timestamp, description, completed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskEntry taskEntry = (TaskEntry) o;
        return timestamp == taskEntry.timestamp &&
                completed == taskEntry.completed &&
                id.equals(taskEntry.id) &&
                creator.equals(taskEntry.creator) &&
                description.equals(taskEntry.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creator, timestamp, description, completed);
    }
}
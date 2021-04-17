package com.gmail.comcorecrew.comcore.server.entry;

import com.gmail.comcorecrew.comcore.enums.TaskStatus;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;
import com.gmail.comcorecrew.comcore.server.id.UserID;
import com.google.gson.JsonElement;
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
     * The UNIX timestamp representing when the task was last modified.
     */
    public final long timestamp;

    /**
     * The UNIX timestamp representing the deadline of the task, or 0 if there is no deadline.
     * If the task is completed, it will always be 0.
     */
    public final long deadline;

    /**
     * The description of the task.
     */
    public final String description;

    /**
     * Who completed the task, or null if it has not yet been completed.
     */
    public final UserID completer;

    /**
     * Who is assigned to the task, or null if nobody is assigned. If the task is completed, it
     * will always be null.
     */
    public final UserID assigned;

    /**
     * Create a TaskEntry with an owner, a description of the task, and whether it is completed.
     *
     * @param id          the TaskID of the task
     * @param creator     the user that created the task
     * @param timestamp   the timestamp that the task was last modified
     * @param deadline    the deadline for the task (or 0 if none)
     * @param description the description of the task
     * @param completer   who completed the task (or null)
     * @param assigned    who is working on the task (or null)
     */
    public TaskEntry(TaskID id, UserID creator, long timestamp, long deadline, String description,
                     UserID completer, UserID assigned) {
        if (id == null) {
            throw new IllegalArgumentException("TaskID cannot be null");
        } else if (creator == null) {
            throw new IllegalArgumentException("task creator cannot be null");
        } else if (timestamp < 1) {
            throw new IllegalArgumentException("task timestamp cannot be less than 1");
        } else if (deadline < 0) {
            throw new IllegalArgumentException("task deadline cannot be negative");
        } else if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("task description cannot be null or empty");
        }

        // If the task is completed, there should be no deadline or assigned user.
        if (completer != null) {
            deadline = 0;
            assigned = null;
        }

        this.id = id;
        this.creator = creator;
        this.timestamp = timestamp;
        this.deadline = deadline;
        this.description = description;
        this.completer = completer;
        this.assigned = assigned;
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
        long deadline = 0;
        UserID completer = null;
        UserID assigned = null;
        if (completed) {
            completer = new UserID(json.get("completedBy").getAsString());
        } else {
            deadline = json.get("deadline").getAsLong();
            JsonElement inProgress = json.get("inProgress");
            if (!inProgress.isJsonNull()) {
                assigned = new UserID(inProgress.getAsString());
            }
        }
        return new TaskEntry(id, creator, timestamp, deadline, description, completer, assigned);
    }

    /**
     * Get the status of the task.
     *
     * @return the status of the task
     */
    public TaskStatus getStatus() {
        if (completer != null) {
            return TaskStatus.COMPLETED;
        } else if (assigned != null) {
            return TaskStatus.IN_PROGRESS;
        } else {
            return TaskStatus.UNASSIGNED;
        }
    }

    /**
     * Check if this task has a deadline associated with it. If the task is completed, this method
     * will always return false.
     *
     * @return true if there is a deadline, false otherwise
     */
    public boolean hasDeadline() {
        return deadline != 0;
    }

    /**
     * Check if the task is overdue (there is a deadline and it already has passed).
     *
     * @return true if the task is overdue, false otherwise
     */
    public boolean isOverdue() {
        return deadline != 0 && deadline < System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskEntry taskEntry = (TaskEntry) o;
        return timestamp == taskEntry.timestamp &&
                id.equals(taskEntry.id) &&
                creator.equals(taskEntry.creator) &&
                description.equals(taskEntry.description) &&
                Objects.equals(completer, taskEntry.completer) &&
                Objects.equals(assigned, taskEntry.assigned);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creator, timestamp, description, completer, assigned);
    }
}
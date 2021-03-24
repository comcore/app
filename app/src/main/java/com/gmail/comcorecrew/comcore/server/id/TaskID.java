package com.gmail.comcorecrew.comcore.server.id;

import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an identifier for a task in a task list.
 */
public class TaskID {
    /**
     * The task list that this task is in.
     */
    public final TaskListID taskList;

    // TODO determine what other information the server will send

    /**
     * Create a TaskID from a parent task list.
     *
     * @param taskList the parent task list
     */
    public TaskID(TaskListID taskList /* more parameters will be added here */) {
        this.taskList = taskList;
    }

    /**
     * Parse a TaskID from a JsonObject. If specified, the given TaskListID is used. Otherwise,
     * it is taken from the message data.
     *
     * @param taskList the task list which contains the task or null
     * @param json     the data sent by the server
     * @return the TaskID
     */
    public static TaskID fromJson(TaskListID taskList, JsonObject json) {
        if (taskList == null) {
            GroupID group = new GroupID(json.get("group").getAsString());
            taskList = new TaskListID(group, json.get("taskList").getAsString());
        }

        return new TaskID(taskList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskID taskID = (TaskID) o;
        return taskList.equals(taskID.taskList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskList);
    }
}
package com.gmail.comcorecrew.comcore.server.id;

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
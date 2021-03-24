package com.gmail.comcorecrew.comcore.server.id;

import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents an identifier for a task in a task list.
 */
public class TaskID extends ModuleItemID<TaskListID> {
    /**
     * Create a TaskID from a parent task list and a numeric ID.
     *
     * @param taskList the parent task list
     * @param id       the numeric ID
     */
    public TaskID(TaskListID taskList, long id) {
        super(taskList, id);
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

        long id = json.get("id").getAsLong();
        return new TaskID(taskList, id);
    }
}
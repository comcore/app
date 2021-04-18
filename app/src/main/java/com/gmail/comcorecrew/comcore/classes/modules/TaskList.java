package com.gmail.comcorecrew.comcore.classes.modules;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.TaskItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.enums.TaskStatus;
import com.gmail.comcorecrew.comcore.notifications.ScheduledList;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;

import java.util.ArrayList;

public class TaskList extends Module {

    private transient ScheduledList<TaskID, TaskEntry> tasks;

    public TaskList(String name, TaskListID id, Group group) {
        super(name, id, group, Mdid.CTSK);
        tasks = new ScheduledList<>();
    }

    public TaskList(String name, Group group) {
        super(name, group, Mdid.CTSK);
        tasks = new ScheduledList<>();
    }

    /**
     * Toggles the completed state of the task with the given id.
     *
     * @param taskID id of the task to toggle
     */
    public void toggleCompleted(TaskID taskID) {
        boolean completed = tasks.get(taskID).getStatus() == TaskStatus.COMPLETED;
        TaskStatus newStatus = completed ? TaskStatus.UNASSIGNED : TaskStatus.COMPLETED;
        ServerConnector.updateTaskStatus(taskID, newStatus, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }

            onTaskUpdated(result.data);
        });
    }

    /**
     * Creates a new task with a given description and sends it to the server,
     * object, and cache.
     *
     * @param deadline    the deadline to set for the task (or 0 for no deadline)
     * @param description description of the task
     */
    public void sendTask(long deadline, String description) {
        ServerConnector.addTask((TaskListID) getId(), deadline, description, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }

            onTaskAdded(result.data);
        });
    }

    /**
     * Deletes a task from the server, object, and cache.
     *
     * @param taskID id of the task to delete
     */
    public void deleteTask(TaskID taskID) {
        ServerConnector.deleteTask(taskID, result -> {
            if (result.isFailure()) {
                ErrorDialog.show(result.errorMessage);
                return;
            }

            onTaskDeleted(taskID);
        });
    }

    public int numEntries() {
        return tasks.size();
    }

    public TaskEntry getEntry(int i) {
        return tasks.get(i);
    }

    @Override
    public void onTaskAdded(TaskEntry task) {
        if (!task.id.module.equals(getId())) {
            return;
        }

        tasks.add(task);
        toCache();
    }

    @Override
    public void onTaskUpdated(TaskEntry task) {
        if (!task.id.module.equals(getId())) {
            return;
        }

        tasks.update(task);
        toCache();
    }

    @Override
    public void onTaskDeleted(TaskID task) {
        if (!task.module.equals(getId())) {
            return;
        }

        if (tasks.remove(task)) {
            toCache();
        }
    }

    @Override
    protected void readToCache() {
        if (tasks.size() == 0) {
            return;
        }

        ArrayList<Cacheable> items = new ArrayList<>();
        for (TaskEntry task : tasks.getEntries()) {
            items.add(new TaskItem(task));
        }
        Cacher.cacheData(items, this);
    }

    @Override
    protected void readFromCache() {
        tasks = new ScheduledList<>();
        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        TaskListID taskList = (TaskListID) getId();
        for (char[] line : data) {
            tasks.add(new TaskItem(line).toEntry(taskList));
        }
    }

    /**
     * Refreshes data from the server into the object and the cache.
     */
    @Override
    public void refresh() {
        ServerConnector.getTasks((TaskListID) getId(), result -> {
            if (result.isFailure()) {
                return;
            }

            tasks.clear();
            for (TaskEntry taskEntry : result.data) {
                tasks.add(taskEntry);
            }
            toCache();
        });
    }
}

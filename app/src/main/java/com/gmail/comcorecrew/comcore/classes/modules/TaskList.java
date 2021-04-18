package com.gmail.comcorecrew.comcore.classes.modules;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.TaskItem;
import com.gmail.comcorecrew.comcore.caching.UserStorage;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.dialogs.ErrorDialog;
import com.gmail.comcorecrew.comcore.enums.Mdid;
import com.gmail.comcorecrew.comcore.enums.TaskStatus;
import com.gmail.comcorecrew.comcore.notifications.NotificationScheduler;
import com.gmail.comcorecrew.comcore.server.ServerConnector;
import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;

import java.util.ArrayList;

public class TaskList extends Module {

    private transient ArrayList<TaskItem> tasks;

    public TaskList(String name, TaskListID id, Group group) {
        super(name, id, group, Mdid.CTSK);
        tasks = new ArrayList<>();
    }

    public TaskList(String name, Group group) {
        super(name, group, Mdid.CTSK);
        tasks = new ArrayList<>();
    }

    /**
     * Returns the data of task items in the object
     *
     * @return the array of task items in the task list.
     */
    public ArrayList<TaskItem> getTasks() {
        return tasks;
    }

    /**
     * Sets the data in the object and the cache to the given list of task items.
     *
     * @param tasks list of task items to replace the tasks
     */
    public void setTasks(ArrayList<TaskItem> tasks) {
        this.tasks = tasks;
        registerNotifications();
        toCache();
    }

    /**
     * Returns the index of the task with the given task id.
     *
     * @param taskID id of the requested task index
     * @return       index of the task; -1 if it does not exist
     */
    public int getTaskIndex(TaskID taskID) {
        long tId = taskID.id;
        for (int i = 0; i < tasks.size(); i++) {
            if (tId == tasks.get(i).getTaskid()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Fetches the task entry of the task with the given task id.
     *
     * @param taskID id of the task to fetch
     * @return       the TaskEntry corresponding to the task; null if it does not exist
     */
    public TaskEntry getTaskEntry(TaskID taskID) {
        int index = getTaskIndex(taskID);
        if (index == -1) {
            return null;
        }
        return tasks.get(index).toEntry((TaskListID) getId());
    }

    /**
     * Toggles the completed state of the task with the given id.
     *
     * @param taskID id of the task to toggle
     */
    public void toggleCompleted(TaskID taskID) {
        int index = getTaskIndex(taskID);
        if (index != -1) {
            boolean completed = !tasks.get(index).isCompleted();
            ServerConnector.updateTaskStatus(taskID, completed ? TaskStatus.COMPLETED : TaskStatus.UNASSIGNED, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(result.errorMessage);
                    return;
                }

                onTaskUpdated(result.data);
            });
        }
    }

    /**
     * Toggles the assigned state of the task with the given id.
     *
     * @param taskID id of the task to toggle
     */
    public void toggleAssigned(TaskID taskID) {
        int index = getTaskIndex(taskID);
        if (index != -1) {
            boolean assigned = !tasks.get(index).isAssigned();
            ServerConnector.updateTaskStatus(taskID, assigned ? TaskStatus.IN_PROGRESS : TaskStatus.UNASSIGNED, result -> {
                if (result.isFailure()) {
                    ErrorDialog.show(result.errorMessage);
                    return;
                }

                tasks.set(index, new TaskItem(result.data));
                toCache();
            });
        }
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

            addTask(result.data);
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

    /**
     * Adds a TaskEntry to the object and the cache.
     *
     * @param entry entry to add
     */
    public void addTask(TaskEntry entry) {
        tasks.add(new TaskItem(entry));
        NotificationScheduler.add(entry);
        toCache();
    }

    /**
     * Adds an array list of TaskEntry's to the object and the cache.
     *
     * @param entries the entries to add
     */
    public void addTasks(ArrayList<TaskEntry> entries) {
        for (TaskEntry entry : entries) {
            tasks.add(new TaskItem(entry));
            NotificationScheduler.add(entry);
        }
        toCache();
    }

    public int numEntries() {
        return tasks.size();
    }

    public TaskEntry getEntry(int i) {
        return tasks.get(i).toEntry((TaskListID) getId());
    }

    /**
     * Returns the data in the object as an array list of task entries.
     *
     * @return the task data as task entries
     */
    public ArrayList<TaskEntry> getTaskEntries() {
        ArrayList<TaskEntry> entries = new ArrayList<>();
        for (TaskItem item : tasks) {
            entries.add(item.toEntry((TaskListID) getId()));
        }
        return entries;
    }

    @Override
    public void onTaskAdded(TaskEntry task) {
        if (!task.id.module.equals(getId())) {
            return;
        }

        addTask(task);
    }

    @Override
    public void onTaskUpdated(TaskEntry task) {
        if (!task.id.module.equals(getId())) {
            return;
        }

        long id = task.id.id;
        for (TaskItem item : tasks) {
            if (item.getTaskid() == id) {
                item.setTimestamp(task.timestamp);
                item.setData(task.description);
                item.setCompleterId(UserStorage.getInternalId(task.completer));
                item.setAssignedId(UserStorage.getInternalId(task.assigned));
                NotificationScheduler.add(task);
                this.toCache();
                return;
            }
        }
    }

    @Override
    public void onTaskDeleted(TaskID task) {
        if (!task.module.equals(getId())) {
            return;
        }

        long id = task.id;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getTaskid() == id) {
                tasks.remove(i);
                NotificationScheduler.remove(task);
                toCache();
                return;
            }
        }
    }

    @Override
    protected void readToCache() {
        if (tasks.size() == 0) {
            return;
        }

        Cacher.cacheData(new ArrayList<>(tasks), this);
    }

    @Override
    protected void readFromCache() {
        tasks = new ArrayList<>();
        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        for (char[] line : data) {
            tasks.add(new TaskItem(line));
        }
        registerNotifications();
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
                tasks.add(new TaskItem(taskEntry));
            }
            registerNotifications();
            toCache();
        });
    }

    private void registerNotifications() {
        for (TaskItem task : tasks) {
            NotificationScheduler.add(task.toEntry((TaskListID) getId()));
        }
    }
}

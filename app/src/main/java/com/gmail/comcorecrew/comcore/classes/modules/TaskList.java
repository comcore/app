package com.gmail.comcorecrew.comcore.classes.modules;

import com.gmail.comcorecrew.comcore.abstracts.Module;
import com.gmail.comcorecrew.comcore.caching.Cacheable;
import com.gmail.comcorecrew.comcore.caching.Cacher;
import com.gmail.comcorecrew.comcore.caching.MsgCacheable;
import com.gmail.comcorecrew.comcore.caching.TaskItem;
import com.gmail.comcorecrew.comcore.classes.Group;
import com.gmail.comcorecrew.comcore.enums.Mdid;
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

    public ArrayList<TaskItem> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<TaskItem> tasks) {
        this.tasks = tasks;
    }

    public TaskItem getTask(TaskID taskID) {
        long tId = taskID.id;
        for (TaskItem item : tasks) {
            if (tId == item.getTaskid()) {
                return item;
            }
        }
        return null;
    }

    public TaskEntry getTaskEntry(TaskID taskID) {
        TaskItem taskItem = getTask(taskID);
        if (taskItem == null) {
            return null;
        }
        return taskItem.toEntry((TaskListID) getId());
    }

    public void toggleCompleted(TaskID taskID) {
        TaskItem taskItem = getTask(taskID);
        if (taskItem != null) {
            taskItem.setCompleted(!taskItem.isCompleted());
            toCache();
        }
    }

    public void deleteTask(TaskID taskID) {
        long tId = taskID.id;
        for (TaskItem item : tasks) {
            if (tId == item.getTaskid()) {
                tasks.remove(item);
                toCache();
                return;
            }
        }
    }

    public void addTask(TaskEntry entry) {
        tasks.add(new TaskItem(entry));
        toCache();
    }

    public void addTasks(ArrayList<TaskEntry> entries) {
        for (TaskEntry entry : entries) {
            tasks.add(new TaskItem(entry));
        }
        toCache();
    }

    public ArrayList<TaskEntry> getTaskEntries() {
        ArrayList<TaskEntry> entries = new ArrayList<>();
        for (TaskItem item : tasks) {
            entries.add(item.toEntry((TaskListID) getId()));
        }
        return entries;
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
        char[][] data = Cacher.uncacheData(this);
        if (data == null) {
            return;
        }

        tasks = new ArrayList<>();
        for (char[] line : data) {
            tasks.add(new TaskItem(line));
        }
    }

    public void refreshTasks() {
        ServerConnector.getTasks((TaskListID) getId(), result -> {
            if (result.isFailure()) {
                //TODO Handle failure
                return;
            }

            for (TaskEntry taskEntry : result.data) {
                tasks.add(new TaskItem(taskEntry));
            }
            toCache();
        });
    }
}

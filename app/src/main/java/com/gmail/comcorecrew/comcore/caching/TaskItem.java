package com.gmail.comcorecrew.comcore.caching;

import com.gmail.comcorecrew.comcore.server.entry.TaskEntry;
import com.gmail.comcorecrew.comcore.server.id.TaskID;
import com.gmail.comcorecrew.comcore.server.id.TaskListID;
import com.gmail.comcorecrew.comcore.server.id.UserID;

public class TaskItem implements Cacheable {

    private int userId;
    private long taskid;
    private long timestamp;
    private long deadline;
    private int completerId;
    private int assignedId;
    private String data;

    public TaskItem(TaskEntry entry) {
        userId = UserStorage.getInternalId(entry.creator);
        taskid = entry.id.id;
        timestamp = entry.timestamp;
        deadline = entry.deadline;
        completerId = entry.completer == null ? -1 : UserStorage.getInternalId(entry.completer);
        assignedId = entry.assigned == null ? -1 : UserStorage.getInternalId(entry.assigned);
        data = entry.description;
    }

    public int getId() {
        return userId;
    }

    public long getTaskid() {
        return taskid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDeadline() {
        return deadline;
    }

    public boolean isCompleted() {
        return completerId != -1;
    }

    public boolean isAssigned() {
        return assignedId != -1;
    }

    public String getData() {
        return data;
    }

    public int getCompleterId() {
        return completerId;
    }

    public int getAssignedId() {
        return assignedId;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setId(int id) {
        this.userId = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public void setTaskid(long taskid) {
        this.taskid = taskid;
    }

    public void setCompleterId(int completerId) {
        this.completerId = completerId;
    }

    public void setAssignedId(int assignedId) {
        this.assignedId = assignedId;
    }

    public TaskEntry toEntry(TaskListID listID) {
        TaskID taskID = new TaskID(listID, taskid);

        return new TaskEntry(taskID, UserStorage.getUser(userId).getID(), timestamp, deadline, data,
                isCompleted() ? UserStorage.getUser(completerId).getID() : null,
                assignedId >= 0 ? UserStorage.getUser(assignedId).getID() : null);
    }

    public int charLength() {
        int total = minLength();
        total += data.length();
        return total;
    }

    public static int minLength() {
        int total = 0;
        total += 2; //userId
        total += 4; //taskId
        total += 4; //timestamp
        total += 4; //deadline
        total += 2; //completerId
        total += 2; //assignedId
        return total;
    }

    @Override
    public char[] toCache() {
        char[] cache = new char[charLength()];

        int index = 0;

        cache[index++] = (char) (userId >> 16);
        cache[index++] = (char) userId;
        cache[index++] = (char) (taskid >> 48);
        cache[index++] = (char) (taskid >> 32);
        cache[index++] = (char) (taskid >> 16);
        cache[index++] = (char) taskid;
        cache[index++] = (char) (timestamp >> 48);
        cache[index++] = (char) (timestamp >> 32);
        cache[index++] = (char) (timestamp >> 16);
        cache[index++] = (char) timestamp;
        cache[index++] = (char) (deadline>> 48);
        cache[index++] = (char) (deadline >> 32);
        cache[index++] = (char) (deadline >> 16);
        cache[index++] = (char) deadline;
        cache[index++] = (char) (completerId >> 16);
        cache[index++] = (char) completerId;
        cache[index++] = (char) (assignedId >> 16);
        cache[index++] = (char) assignedId;
        for (int i = 0; i < data.length(); i++) {
            cache[index++] = data.charAt(i);
        }

        return cache;
    }

    public TaskItem(char[] cache) {
        if (cache.length < minLength()) { //Makes sure the array length is valid.
            throw new IllegalArgumentException();
        }

        int index = 0;

        //Reads the array into the object.
        userId = cache[index++];
        userId = (userId << 16) | cache[index++];
        taskid = cache[index++];
        taskid = (taskid << 16) | cache[index++];
        taskid = (taskid << 16) | cache[index++];
        taskid = (taskid << 16) | cache[index++];
        timestamp = cache[index++];
        timestamp = (timestamp << 16) | cache[index++];
        timestamp = (timestamp << 16) | cache[index++];
        timestamp = (timestamp << 16) | cache[index++];
        deadline = cache[index++];
        deadline = (deadline << 16) | cache[index++];
        deadline = (deadline << 16) | cache[index++];
        deadline = (deadline << 16) | cache[index++];
        completerId = cache[index++];
        completerId = (completerId << 16) | cache[index++];
        assignedId = cache[index++];
        assignedId = (assignedId << 16) | cache[index++];
        data = new String(cache, index, cache.length - index);

    }
}

package com.gmail.comcorecrew.comcore.enums;

/**
 * Represents the status of a task in a task list.
 */
public enum TaskStatus {
    /**
     * A task which hasn't been claimed by a user or completed.
     */
    UNASSIGNED,

    /**
     * A task which has been claimed by a user but hasn't been completed.
     */
    IN_PROGRESS,

    /**
     * A task which has been completed.
     */
    COMPLETED;
}
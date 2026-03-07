package com.unitrack.backend.tasks.events;

import java.util.UUID;

public class TaskCreatedEvent {

    private final UUID taskId;
    private final UUID userId;

    public TaskCreatedEvent(UUID taskId, UUID userId) {
        this.taskId = taskId;
        this.userId = userId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getUserId() {
        return userId;
    }

}

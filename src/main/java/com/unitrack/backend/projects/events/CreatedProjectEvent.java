package com.unitrack.backend.projects.events;

import java.util.UUID;

public class CreatedProjectEvent {

    private final UUID projectId;
    private final UUID userId;

    public CreatedProjectEvent(UUID projectId, UUID userId) {
        this.projectId = projectId;
        this.userId = userId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getUserId() {
        return userId;
    }

}

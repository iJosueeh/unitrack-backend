package com.unitrack.backend.projects.events;

import java.util.UUID;

public class ProjectCreatedEvent {

    private final UUID projectId;
    private final UUID userId;

    public ProjectCreatedEvent(UUID projectId, UUID userId) {
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

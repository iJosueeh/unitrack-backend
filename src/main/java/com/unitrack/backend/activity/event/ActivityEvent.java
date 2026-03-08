package com.unitrack.backend.activity.event;

import java.util.UUID;

import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;

public class ActivityEvent {

    private final UUID userId;
    private final ActivityAction action;
    private final ActivityEntityType entityType;
    private final UUID entityId;

    public ActivityEvent(UUID userId, ActivityAction action, ActivityEntityType entityType, UUID entityId) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public UUID getUserId() {
        return userId;
    }

    public ActivityAction getAction() {
        return action;
    }

    public ActivityEntityType getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

}

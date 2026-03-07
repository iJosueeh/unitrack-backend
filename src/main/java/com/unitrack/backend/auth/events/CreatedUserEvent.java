package com.unitrack.backend.auth.events;

import java.util.UUID;

public class CreatedUserEvent {

    private UUID userId;

    public CreatedUserEvent(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

}
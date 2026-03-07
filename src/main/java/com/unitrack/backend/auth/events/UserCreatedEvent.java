package com.unitrack.backend.auth.events;

import java.util.UUID;

public class UserCreatedEvent {

    private UUID userId;

    public UserCreatedEvent(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

}
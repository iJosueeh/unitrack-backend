package com.unitrack.backend.projects.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectAssignRequest(
        @NotNull(message = "Assigned user ID is required")
        UUID assignedToId
) {}

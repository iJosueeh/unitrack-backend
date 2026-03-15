package com.unitrack.backend.tasks.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TaskAssignRequest(
        @NotNull(message = "assignedToId is required")
        UUID assignedToId
) {}

package com.unitrack.backend.projects.dto;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import jakarta.validation.constraints.NotNull;

public record ProjectStatusUpdateRequest(
        @NotNull(message = "Project status is required")
        Status status,

        Priority priority
) {}
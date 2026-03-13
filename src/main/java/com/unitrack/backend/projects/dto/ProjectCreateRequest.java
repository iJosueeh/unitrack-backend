package com.unitrack.backend.projects.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
        @NotBlank(message = "Project name is required") @Size(min = 2, max = 100, message = "Project name must have between 2 and 100 characters")
        String name,

        @Size(max = 800, message = "Project description must have at most 800 characters")
        String description,

        @Size(max = 120, message = "Client name must have at most 120 characters")
        String client,

        @NotNull(message = "Project status is required")
        Status status,

        @NotNull(message = "Project priority is required")
        Priority priority,

        @NotNull(message = "Project budget is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Project budget must be greater than 0")
        BigDecimal budget,

        @NotNull(message = "Project start date is required")
        Timestamp startDate,

        @NotNull(message = "Project end date is required")
        Timestamp endDate,

        @NotNull(message = "Workspace ID is required")
        UUID workspaceId,

        UUID assignedToId) {}
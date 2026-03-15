package com.unitrack.backend.tasks.dto;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;
import java.util.UUID;

public record TaskCreateRequest(
        @NotBlank(message = "Task title is required")
        @Size(min = 2, max = 160, message = "Task title must be between 2 and 160 characters")
        String title,

        @Size(max = 1000, message = "Task description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Task status is required")
        Status status,

        @NotNull(message = "Task priority is required")
        Priority priority,
        Timestamp dueDate,
        UUID assignedToId
) {}

package com.unitrack.backend.tasks.dto;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;
import java.util.UUID;

public record TaskUpdateRequest(
        @Size(min = 2, max = 160, message = "Title must be between 2 and 160 characters")
        String title,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        Status status,
        Priority priority,
        Timestamp dueDate,
        UUID assignedToId
) {}

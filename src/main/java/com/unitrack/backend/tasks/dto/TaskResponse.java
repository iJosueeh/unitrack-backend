package com.unitrack.backend.tasks.dto;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        Status status,
        Priority priority,
        Timestamp dueDate,
        UUID projectId,
        UUID createdById,
        UUID assignedToId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

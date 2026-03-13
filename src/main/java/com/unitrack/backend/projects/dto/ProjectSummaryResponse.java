package com.unitrack.backend.projects.dto;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectSummaryResponse(
        UUID id,
        String name,
        String client,
        Status status,
        Priority priority,
        Timestamp startDate,
        Timestamp endDate,
        UUID assignedToId,
        String assignedToName,
        UUID createdById,
        String createdByName,
        long tasksCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

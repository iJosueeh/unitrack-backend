package com.unitrack.backend.projects.dto;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        String client,

        Status status,
        Priority priority,

        BigDecimal budget,
        Timestamp startDate,
        Timestamp endDate,

        UUID createdById,
        UUID assignedToId,
        UUID workspaceId,

        LocalDateTime createdAt,
        LocalDateTime updatedAt


) {}

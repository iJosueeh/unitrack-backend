package com.unitrack.backend.projects.dto;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

public record ProjectUpdateRequest(
        @Size(min = 2, max = 120, message = "Project name must have between 2 and 120 characters")
        String name,

        @Size(max = 800, message = "Project description must have at most 800 characters")
        String description,

        @Size(max = 120, message = "Client name must have at most 120 characters")
        String client,
        Status status,
        Priority priority,

        @DecimalMin(value = "0.0", inclusive = false, message = "Project budget must be greater than 0")
        BigDecimal budget,

        Timestamp startDate,
        Timestamp endDate,

        UUID assignedToId) {}
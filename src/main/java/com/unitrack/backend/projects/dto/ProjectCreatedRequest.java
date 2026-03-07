package com.unitrack.backend.projects.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ProjectCreatedRequest {

    private String name;
    private String client;
    private BigDecimal budget;
    private Timestamp startDate;
    private Timestamp endDate;
    private String description;
    private UUID createdBy;
    private UUID workspaceId;

}

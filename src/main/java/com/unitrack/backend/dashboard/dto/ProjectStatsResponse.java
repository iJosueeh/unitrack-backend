package com.unitrack.backend.dashboard.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ProjectStatsResponse {

    private UUID id;
    private UUID projectId;
    private Integer tasksCompletedThisWeek;
    private Integer hoursRegisteredThisWeek;
    private Integer totalTasksCompleted;
    private BigDecimal totalHoursRegistered;
    private BigDecimal overallProgressPercentage;
    private Timestamp lastUpdatedAt;

}

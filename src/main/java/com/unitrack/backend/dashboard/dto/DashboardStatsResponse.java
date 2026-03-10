package com.unitrack.backend.dashboard.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {

    private UUID id;
    private UUID workspaceId;
    private Integer projectsCountActive;
    private Integer projectsActiveDelta;
    private Integer taskPendingCount;
    private BigDecimal overallProgressPercentage;
    private Integer membersCount;
    private Integer membersOnsiteToday;

}

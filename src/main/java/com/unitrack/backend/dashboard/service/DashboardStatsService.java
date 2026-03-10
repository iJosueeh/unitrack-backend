package com.unitrack.backend.dashboard.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unitrack.backend.dashboard.dto.DashboardStatsResponse;
import com.unitrack.backend.dashboard.entity.DashboardStats;
import com.unitrack.backend.dashboard.repository.DashboardStatsRepository;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardStatsService {

    private final DashboardStatsRepository dashboardStatsRepository;
    private final WorkspaceRepository workspaceRepository;

    public DashboardStats createdDashStats(UUID workspaceId) {
        if (workspaceId == null) {
            log.error("Workspace ID is null. Cannot create DashboardStats.");
            throw new IllegalArgumentException("Workspace ID cannot be null");
        }

        DashboardStats dashStats = new DashboardStats();
        dashStats.setWorkspace(workspaceRepository.findById(workspaceId).orElse(null));
        dashStats.setProjectsCountActive(0);
        dashStats.setProjectsActiveDelta(0);
        dashStats.setTaskPendingCount(0);
        dashStats.setOverallProgressPercentage(BigDecimal.ZERO);
        dashStats.setMembersCount(0);
        dashStats.setMembersOnsiteToday(0);

        DashboardStats savedStats = dashboardStatsRepository.save(dashStats);
        log.info("DashboardStats created with ID: {} for Workspace ID: {}", savedStats.getId(), workspaceId);
        return savedStats;
    }

    public DashboardStatsResponse mapToResponse(DashboardStats body) {
        return DashboardStatsResponse.builder()
                .id(body.getId())
                .workspaceId(body.getWorkspace().getId())
                .projectsCountActive(body.getProjectsCountActive())
                .projectsActiveDelta(body.getProjectsActiveDelta())
                .taskPendingCount(body.getTaskPendingCount())
                .overallProgressPercentage(body.getOverallProgressPercentage())
                .membersCount(body.getMembersCount())
                .membersOnsiteToday(body.getMembersOnsiteToday())
                .build();
    }

}

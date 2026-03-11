package com.unitrack.backend.dashboard.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitrack.backend.dashboard.dto.ProjectStatsResponse;
import com.unitrack.backend.dashboard.entity.ProjectStats;
import com.unitrack.backend.dashboard.repository.ProjectStatsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectStatsService {

    private final ProjectStatsRepository projectStatsRepository;

    @Transactional
    public ProjectStats createdProjectStats(UUID projectId) {
        if (projectId == null) {
            log.error("Project ID is null");
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        ProjectStats projectStats = new ProjectStats();
        projectStats.setProject(projectStatsRepository.findByProject_Id(projectId).orElse(null).getProject());
        projectStats.setTasksCompletedThisWeek(0);
        projectStats.setHoursRegisteredThisWeek(0);
        projectStats.setTotalTasksCompleted(0);
        projectStats.setTotalHoursRegistered(BigDecimal.ZERO);
        projectStats.setOverallProgressPercentage(BigDecimal.ZERO);
        projectStats.setLastUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return projectStatsRepository.save(projectStats);
    }

    public ProjectStatsResponse mapToResponse(ProjectStats body) {
        return ProjectStatsResponse.builder()
                .id(body.getId())
                .projectId(body.getProject().getId())
                .tasksCompletedThisWeek(body.getTasksCompletedThisWeek())
                .hoursRegisteredThisWeek(body.getHoursRegisteredThisWeek())
                .totalTasksCompleted(body.getTotalTasksCompleted())
                .totalHoursRegistered(body.getTotalHoursRegistered())
                .overallProgressPercentage(body.getOverallProgressPercentage())
                .lastUpdatedAt(body.getLastUpdatedAt())
                .build();
    }

}

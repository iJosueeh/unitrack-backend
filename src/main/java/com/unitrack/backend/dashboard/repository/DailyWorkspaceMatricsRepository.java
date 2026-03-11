package com.unitrack.backend.dashboard.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.dashboard.entity.DailyWorkspaceMetrics;

public interface DailyWorkspaceMatricsRepository extends JpaRepository<DailyWorkspaceMetrics, UUID> {

    List<DailyWorkspaceMetrics> findByWorkspace_IdOrderByDateDesc(UUID workspaceId);

    List<DailyWorkspaceMetrics> findByWorkspace_IdAndDateBetween(UUID workspaceId, Timestamp startDate, Timestamp endDate);

}

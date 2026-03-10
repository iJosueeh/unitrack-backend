package com.unitrack.backend.dashboard.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.dashboard.entity.DashboardStats;

public interface DashboardStatsRepository extends JpaRepository<DashboardStats, UUID> {
    Optional<DashboardStats> findByWorkspace_Id(UUID workspaceId);
}

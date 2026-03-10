package com.unitrack.backend.dashboard.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.dashboard.entity.ProjectStats;

public interface ProjectStatsRepository extends JpaRepository<ProjectStats, UUID> {
    Optional<ProjectStats> findByProject_Id(UUID projectId);
}

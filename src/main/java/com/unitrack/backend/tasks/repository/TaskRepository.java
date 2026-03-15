package com.unitrack.backend.tasks.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.tasks.entity.Tasks;

public interface TaskRepository extends JpaRepository<Tasks, UUID> {
    Optional<Tasks> findByIdAndProject_IdAndProject_Workspaces_Id(
            UUID taskId, UUID projectId, UUID workspaceId);

    List<Tasks> findByProject_IdAndProject_Workspaces_IdOrderByCreatedAtDesc(
            UUID projectId, UUID workspaceId);

    long countByProject_Id(UUID projectId);
}

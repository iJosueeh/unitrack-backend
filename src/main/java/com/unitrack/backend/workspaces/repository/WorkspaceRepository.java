package com.unitrack.backend.workspaces.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.workspaces.entity.Workspaces;

public interface WorkspaceRepository extends JpaRepository<Workspaces, UUID> {

    Workspaces findByName(String name);
    Workspaces findByIdAndOwnerId(UUID id, UUID ownerId);
    Long countByOwnerId(UUID ownerId);
}

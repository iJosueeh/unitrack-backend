package com.unitrack.backend.workspaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.workspaces.entity.WorkspaceInvite;

public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInvite, UUID> {

    Optional<WorkspaceInvite> findByCodeHash(String codeHash);

    List<WorkspaceInvite> findByWorkspaces_IdAndIsActiveTrue(UUID workspaceId);

}

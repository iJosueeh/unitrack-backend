package com.unitrack.backend.workspaces.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.workspaces.entity.WorkspacesMembers;

public interface WorkspacesMembersRepository extends JpaRepository<WorkspacesMembers, UUID> {
    List<WorkspacesMembers> findByWorkspaces_Id(UUID workspaceId);

    List<WorkspacesMembers> findByUser_Id(UUID userId);

}

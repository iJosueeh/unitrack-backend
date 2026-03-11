package com.unitrack.backend.workspaces.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.unitrack.backend.workspaces.entity.Workspaces;

public interface WorkspaceRepository extends JpaRepository<Workspaces, UUID> {

    Workspaces findByName(String name);

    Workspaces findByIdAndOwnerId_Id(UUID id, UUID ownerId);

    long countByOwnerId_Id(UUID ownerId);

    @Query("""
        select w.id as id,
           w.name as name,
           w.ownerId.id as ownerId,
           count(distinct wmAll.id) as membersCount,
           count(distinct p.id) as projectsCount,
           w.createdAt as createdAt,
           w.updatedAt as updatedAt
        from Workspaces w
        join w.members wmSelf on wmSelf.user.id = :userId
        left join w.members wmAll
        left join w.projects p
        where w.id = :workspaceId
        group by w.id, w.name, w.ownerId.id, w.createdAt, w.updatedAt
        """)
    Optional<WorkspaceSummaryView> findWorkspaceSummaryByIdForUser(@Param("workspaceId") UUID workspaceId,
        @Param("userId") UUID userId);

    @Query("""
        select w.id as id,
           w.name as name,
           w.ownerId.id as ownerId,
           count(distinct wmAll.id) as membersCount,
           count(distinct p.id) as projectsCount,
           w.createdAt as createdAt,
           w.updatedAt as updatedAt
        from Workspaces w
        join w.members wmSelf on wmSelf.user.id = :userId
        left join w.members wmAll
        left join w.projects p
        group by w.id, w.name, w.ownerId.id, w.createdAt, w.updatedAt
        """)
    List<WorkspaceSummaryView> findWorkspaceSummariesByUserId(@Param("userId") UUID userId);
}

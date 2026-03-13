package com.unitrack.backend.projects.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unitrack.backend.projects.entity.Projects;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectsRepository extends JpaRepository<Projects, UUID> {
    Optional<Projects> findByIdAndWorkspaces_Id(UUID projectId, UUID workspaceId);
    List<Projects> findByWorkspaces_IdOrderByCreatedAtDesc(UUID workspaceId);
    boolean existsByNameIgnoreCaseAndWorkspaces_Id(String name, UUID workspaceId);

    @Query("""
        select p.id                  as id,
               p.name                as name,
               p.client              as client,
               p.status              as status,
               p.priority            as priority,
               p.startDate           as startDate,
               p.endDate             as endDate,
               at.id                 as assignedToId,
               at.firstName          as assignedToFirstName,
               at.lastName           as assignedToLastName,
               cb.id                 as createdById,
               cb.firstName          as createdByFirstName,
               cb.lastName           as createdByLastName,
               count(distinct t.id)  as tasksCount,
               p.createdAt           as createdAt,
               p.updatedAt           as updatedAt
        from Projects p
        left join p.assignedTo at
        left join p.createdBy  cb
        left join p.tasks      t
        where p.workspaces.id = :workspaceId
        group by p.id, p.name, p.client, p.status, p.priority,
                 p.startDate, p.endDate,
                 at.id, at.firstName, at.lastName,
                 cb.id, cb.firstName, cb.lastName,
                 p.createdAt, p.updatedAt
        order by p.createdAt desc
        """)
    List<ProjectSummaryView> findSummariesByWorkspaceId(@Param("workspaceId") UUID workspaceId);


    @Query("""
            select p.id                  as id,
                   p.name                as name,
                   p.client              as client,
                   p.status              as status,
                   p.priority            as priority,
                   p.startDate           as startDate,
                   p.endDate             as endDate,
                   at.id                 as assignedToId,
                   at.firstName          as assignedToFirstName,
                   at.lastName           as assignedToLastName,
                   cb.id                 as createdById,
                   cb.firstName          as createdByFirstName,
                   cb.lastName           as createdByLastName,
                   count(distinct t.id)  as tasksCount,
                   p.createdAt           as createdAt,
                   p.updatedAt           as updatedAt
            from Projects p
            left join p.assignedTo at
            left join p.createdBy  cb
            left join p.tasks      t
            where p.workspaces.id = :workspaceId
              and p.id = :projectId
            group by p.id, p.name, p.client, p.status, p.priority,
                     p.startDate, p.endDate,
                     at.id, at.firstName, at.lastName,
                     cb.id, cb.firstName, cb.lastName,
                     p.createdAt, p.updatedAt
            """)
    Optional<ProjectSummaryView> findSummaryByIdAndWorkspaceId(
            @Param("projectId")   UUID projectId,
            @Param("workspaceId") UUID workspaceId);
}

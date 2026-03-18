package com.unitrack.backend.projects.service;

import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.common.exception.ConflictException;
import com.unitrack.backend.common.exception.NotFoundException;
import com.unitrack.backend.common.utils.ValidateDateRange;
import com.unitrack.backend.projects.dto.*;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.projects.repository.ProjectSummaryView;
import com.unitrack.backend.projects.repository.ProjectsRepository;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;
import com.unitrack.backend.workspaces.security.WorkspaceAccessPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectsRepository projectsRepository;
    private final CurrentUserService currentUserService;
    private final WorkspaceAccessPolicy workspaceAccessPolicy;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final ValidateDateRange validate;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public ProjectResponse createProject(UUID workspaceId, ProjectCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Project create request cannot be null");
        }

        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        String normalizedName = normalizeRequiredProjectName(request.name());
        if (projectsRepository.existsByNameIgnoreCaseAndWorkspaces_Id(normalizedName, workspaceId)) {
            log.warn("Project name '{}' already exists in workspace '{}'", normalizedName, workspaceId);
            throw new ConflictException("Project name already exists in this workspace");
        }

        validate.validateDateRange(request.startDate(), request.endDate());
        Workspaces workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        User assignedTo = null;
        if (request.assignedToId() != null) {
            workspaceAccessPolicy.requireMembership(workspaceId, request.assignedToId());
            assignedTo = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new NotFoundException("Assigned user not found"));
        }

        Projects project = new Projects();
        project.setName(normalizedName);
        project.setDescription(request.description());
        project.setClient(request.client());
        project.setStatus(request.status());
        project.setPriority(request.priority());
        project.setBudget(request.budget());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setCreatedBy(requester);
        project.setAssignedTo(assignedTo);
        project.setWorkspaces(workspace);

        Projects saved = projectsRepository.save(project);
        publisher.publishEvent(new ActivityEvent(
                requester.getId(),
                ActivityAction.CREATED,
                ActivityEntityType.PROJECT,
                saved.getId()));
        log.info("Project '{}' created with ID '{}' in workspace '{}'", saved.getName(), saved.getId(), workspaceId);
        return mapToProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryResponse> getProjectsByWorkspace(UUID workspaceId) {
        User user = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireMembership(workspaceId, user.getId());

        log.info("Retrieving projects for workspace '{}'", workspaceId);
        return projectsRepository.findSummariesByWorkspaceId(workspaceId)
                .stream()
                .map(this::mapToSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID workspaceId, UUID projectId) {
        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireMembership(workspaceId, requester.getId());

        Projects project = findProjectInWorkspace(projectId, workspaceId);
        log.info("Project '{}' found in workspace '{}'", projectId, workspaceId);
        return mapToProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID workspaceId, UUID projectId, ProjectUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Project update request cannot be null");
        }

        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Projects project = findProjectInWorkspace(projectId, workspaceId);

        if (request.name() != null) {
            String normalizedName = normalizeProjectName(request.name());
            boolean changed = !normalizedName.equalsIgnoreCase(project.getName());
            if (changed && projectsRepository.existsByNameIgnoreCaseAndWorkspaces_Id(normalizedName, workspaceId)) {
                log.warn("Project name '{}' already exists in workspace '{}'", normalizedName, workspaceId);
                throw new ConflictException("Project name already exists in this workspace");
            }
            project.setName(normalizedName);
        }

        if (request.description() != null) project.setDescription(request.description());
        if (request.client() != null) project.setClient(request.client());
        if (request.status() != null) project.setStatus(request.status());
        if (request.priority() != null) project.setPriority(request.priority());
        if (request.budget() != null) project.setBudget(request.budget());

        Timestamp nextStart = request.startDate() != null ? request.startDate() : project.getStartDate();
        Timestamp nextEnd = request.endDate() != null ? request.endDate() : project.getEndDate();
        validate.validateDateRange(nextStart, nextEnd);

        if (request.startDate() != null) project.setStartDate(request.startDate());
        if (request.endDate() != null) project.setEndDate(request.endDate());

        if (request.assignedToId() != null) {
            workspaceAccessPolicy.requireMembership(workspaceId, request.assignedToId());
            User assignedTo = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new NotFoundException("Assigned user not found"));
            project.setAssignedTo(assignedTo);
        }

        Projects saved = projectsRepository.save(project);
        publisher.publishEvent(new ActivityEvent(
                requester.getId(),
                ActivityAction.UPDATED,
                ActivityEntityType.PROJECT,
                saved.getId()));
        log.info("Project '{}' updated in workspace '{}'", projectId, workspaceId);
        return mapToProjectResponse(saved);
    }

    @Transactional
    public ProjectResponse assignProjectMember(UUID workspaceId, UUID projectId, ProjectAssignRequest request) {
        if (request == null || request.assignedToId() == null) {
            throw new IllegalArgumentException("Assigned user ID is required");
        }

        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Projects project = findProjectInWorkspace(projectId, workspaceId);

        workspaceAccessPolicy.requireMembership(workspaceId, request.assignedToId());
        User assignedTo = userRepository.findById(request.assignedToId())
                .orElseThrow(() -> new NotFoundException("Assigned user not found"));

        project.setAssignedTo(assignedTo);
        Projects saved = projectsRepository.save(project);
        publisher.publishEvent(new ActivityEvent(
                requester.getId(),
                ActivityAction.ASSIGN,
                ActivityEntityType.PROJECT,
                saved.getId()));
        log.info("User '{}' assigned to project '{}' in workspace '{}'", request.assignedToId(), projectId, workspaceId);
        return mapToProjectResponse(saved);
    }

    @Transactional
    public ProjectResponse unassignProjectMember(UUID workspaceId, UUID projectId) {
        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Projects project = findProjectInWorkspace(projectId, workspaceId);
        if (project.getAssignedTo() == null) {
            log.info("Project '{}' in workspace '{}' is already unassigned", projectId, workspaceId);
            return mapToProjectResponse(project);
        }

        project.setAssignedTo(null);
        Projects saved = projectsRepository.save(project);
        publisher.publishEvent(new ActivityEvent(
                requester.getId(),
                ActivityAction.ASSIGN,
                ActivityEntityType.PROJECT,
                saved.getId()));
        log.info("Project '{}' unassigned successfully in workspace '{}'", projectId, workspaceId);
        return mapToProjectResponse(saved);
    }

    @Transactional
    public ProjectResponse updateProjectStatus(UUID workspaceId, UUID projectId, ProjectStatusUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Project status update request cannot be null");
        }
        if (request.status() == null) {
            throw new IllegalArgumentException("Project status is required");
        }

        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Projects project = findProjectInWorkspace(projectId, workspaceId);
        project.setStatus(request.status());
        if (request.priority() != null) project.setPriority(request.priority());

        Projects saved = projectsRepository.save(project);
        publisher.publishEvent(new ActivityEvent(
                requester.getId(),
                ActivityAction.UPDATED,
                ActivityEntityType.PROJECT,
                saved.getId()));
        log.info("Project '{}' status updated to '{}' in workspace '{}'", projectId, request.status(), workspaceId);
        return mapToProjectResponse(saved);
    }

    private Projects findProjectInWorkspace(UUID projectId, UUID workspaceId) {
        return projectsRepository.findByIdAndWorkspaces_Id(projectId, workspaceId)
                .orElseThrow(() -> new NotFoundException("Project not found in this workspace"));
    }

    private String normalizeRequiredProjectName(String rawName) {
        if (rawName == null) throw new IllegalArgumentException("Project name is required");
        return normalizeProjectName(rawName);
    }

    private String normalizeProjectName(String rawName) {
        String normalized = rawName.trim();
        if (normalized.isBlank()) throw new IllegalArgumentException("Project name is required");
        return normalized;
    }

    private ProjectResponse mapToProjectResponse(Projects project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getClient(),
                project.getStatus(),
                project.getPriority(),
                project.getBudget(),
                project.getStartDate(),
                project.getEndDate(),
                project.getCreatedBy() != null ? project.getCreatedBy().getId() : null,
                project.getAssignedTo() != null ? project.getAssignedTo().getId() : null,
                project.getWorkspaces().getId(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private ProjectSummaryResponse mapToSummaryResponse(ProjectSummaryView view) {
        String assignedToName = view.getAssignedToId() != null
                ? view.getAssignedToFirstName() + " " + view.getAssignedToLastName()
                : null;
        String createdByName = view.getCreatedById() != null
                ? view.getCreatedByFirstName() + " " + view.getCreatedByLastName()
                : null;

        return new ProjectSummaryResponse(
                view.getId(),
                view.getName(),
                view.getClient(),
                view.getStatus(),
                view.getPriority(),
                view.getStartDate(),
                view.getEndDate(),
                view.getAssignedToId(),
                assignedToName,
                view.getCreatedById(),
                createdByName,
                view.getTasksCount(),
                view.getCreatedAt(),
                view.getUpdatedAt()
        );
    }
}
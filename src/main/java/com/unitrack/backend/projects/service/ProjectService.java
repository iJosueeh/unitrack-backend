package com.unitrack.backend.projects.service;

import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.common.utils.ValidateDateRange;
import com.unitrack.backend.projects.dto.*;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.projects.repository.ProjectSummaryView;
import com.unitrack.backend.projects.repository.ProjectsRepository;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.entity.WorkspacesMembers;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;
import com.unitrack.backend.workspaces.repository.WorkspacesMembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectsRepository projectsRepository;
    private final CurrentUserService currentUserService;
    private final WorkspacesMembersRepository workspacesMembersRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private ValidateDateRange validate;


    public ProjectResponse createProject(ProjectCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Project create request cannot be null");
        }

        UUID workspaceId = request.workspaceId();
        User requester = currentUserService.getAuthenticatedUser();
        WorkspacesMembers membership = requiredMembership(workspaceId, requester.getId());
        validateCanWrite(membership);

        String normalizedName = request.name().trim();
        if (projectsRepository.existsByNameIgnoreCaseAndWorkspaces_Id(normalizedName, workspaceId)) {
            throw new IllegalArgumentException("Project name already exists in this workspace");
        }

        validate.validateDateRange(request.startDate(), request.endDate());
        Workspaces workspaces = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        User assignedTo = null;
        if (request.assignedToId() != null) {
            requiredMembership(workspaceId, request.assignedToId());
            assignedTo = userRepository.findById(request.assignedToId())
                    .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));
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
        project.setWorkspaces(workspaces);

        Projects savedProject = projectsRepository.save(project);
        return mapToProjectResponse(savedProject);
    }

    public List<ProjectSummaryResponse> getProjectsByWorkspace(UUID workspaceId) {
        return null; // TODO: Implement logic to fetch project summaries for a workspace
    }

    public ProjectResponse getProjectById(UUID workspaceId, UUID projectId) {
        return null; // TODO: Implement logic to fetch a project by its ID within a workspace
    }

    public ProjectResponse updateProject(UUID workspaceId, UUID projectId, ProjectUpdateRequest request) {
        return null; // TODO: implement logic to update a project by its ID within a workspace
    }

    public ProjectResponse assignUser(UUID workspaceId, UUID projectId, ProjectAssignRequest request) {
        return null; // TODO: implement logic to assign a user to a project
    }

    public ProjectResponse updateStatus(UUID workspaceId, UUID projectId, ProjectStatusUpdateRequest request) {
        return null; // TODO: implement logic to update the status of a project
    }

    private WorkspacesMembers requiredMembership(UUID workspaceId, UUID userId) {
        if (workspaceId == null) {
            throw new IllegalArgumentException("Workspace ID is required");
        }

        WorkspacesMembers membership = workspacesMembersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId);
        if (membership == null) {
            throw new AccessDeniedException("User is not a member of this workspace");
        }
        return membership;
    }

    private void validateCanWrite(WorkspacesMembers membership) {
        WorkspaceRole role = membership.getRole();
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new AccessDeniedException("Only OWNER or ADMIN can manage projects");
        }
    }

    private ProjectResponse mapToProjectResponse(Projects request) {
        return new ProjectResponse(
                request.getId(),
                request.getName(),
                request.getDescription(),
                request.getClient(),
                request.getStatus(),
                request.getPriority(),
                request.getBudget(),
                request.getStartDate(),
                request.getEndDate(),
                request.getAssignedTo() != null ? request.getAssignedTo().getId() : null,
                request.getCreatedBy() != null ? request.getCreatedBy().getId() : null,
                request.getWorkspaces().getId(),
                request.getCreatedAt(),
                request.getUpdatedAt()
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

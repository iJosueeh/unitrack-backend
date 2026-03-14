package com.unitrack.backend.projects.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import com.unitrack.backend.common.utils.ValidateDateRange;
import com.unitrack.backend.projects.dto.ProjectAssignRequest;
import com.unitrack.backend.projects.dto.ProjectCreateRequest;
import com.unitrack.backend.projects.dto.ProjectResponse;
import com.unitrack.backend.projects.dto.ProjectStatusUpdateRequest;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.projects.repository.ProjectsRepository;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.entity.WorkspacesMembers;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;
import com.unitrack.backend.workspaces.security.WorkspaceAccessPolicy;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectsRepository projectsRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private WorkspaceAccessPolicy workspaceAccessPolicy;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidateDateRange validate;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void getProjectById_ShouldFail_WhenRequesterIsNotWorkspaceMember() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        doThrow(new AccessDeniedException("User is not a member of this workspace"))
                .when(workspaceAccessPolicy).requireMembership(workspaceId, requesterId);

        assertThrows(AccessDeniedException.class, () -> projectService.getProjectById(workspaceId, projectId));

        verify(projectsRepository, never()).findByIdAndWorkspaces_Id(any(), any());
    }

    @Test
    void getProjectById_ShouldMapCreatedByAndAssignedToCorrectly() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID assignedToId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        User createdBy = new User();
        createdBy.setId(createdById);

        User assignedTo = new User();
        assignedTo.setId(assignedToId);

        Projects project = project(projectId, workspaceId, createdBy, assignedTo);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(projectsRepository.findByIdAndWorkspaces_Id(projectId, workspaceId)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(workspaceId, projectId);

        assertEquals(createdById, response.createdById());
        assertEquals(assignedToId, response.assignedToId());
    }

    @Test
    void assignProjectMember_ShouldFail_WhenAssigneeIsNotWorkspaceMember() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        Projects project = project(projectId, workspaceId, requester, null);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(projectsRepository.findByIdAndWorkspaces_Id(projectId, workspaceId)).thenReturn(Optional.of(project));
        doThrow(new AccessDeniedException("User is not a member of this workspace"))
                .when(workspaceAccessPolicy).requireMembership(workspaceId, assigneeId);

        assertThrows(AccessDeniedException.class, () -> projectService.assignProjectMember(
                workspaceId, projectId, new ProjectAssignRequest(assigneeId)));

        verify(userRepository, never()).findById(assigneeId);
        verify(projectsRepository, never()).save(any(Projects.class));
    }

    @Test
    void unassignProjectMember_ShouldClearAssignee_WhenProjectHasAssignee() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID createdById = UUID.randomUUID();
        UUID assignedToId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        User createdBy = new User();
        createdBy.setId(createdById);

        User assignedTo = new User();
        assignedTo.setId(assignedToId);

        Projects project = project(projectId, workspaceId, createdBy, assignedTo);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(projectsRepository.findByIdAndWorkspaces_Id(projectId, workspaceId)).thenReturn(Optional.of(project));
        when(projectsRepository.save(any(Projects.class))).thenAnswer(i -> i.getArgument(0));

        ProjectResponse response = projectService.unassignProjectMember(workspaceId, projectId);

        assertNull(response.assignedToId());
        assertEquals(createdById, response.createdById());
        verify(projectsRepository).save(project);
    }

    @Test
    void unassignProjectMember_ShouldBeIdempotent_WhenProjectIsAlreadyUnassigned() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        Projects project = project(projectId, workspaceId, requester, null);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(projectsRepository.findByIdAndWorkspaces_Id(projectId, workspaceId)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.unassignProjectMember(workspaceId, projectId);

        assertNull(response.assignedToId());
        verify(projectsRepository, never()).save(any(Projects.class));
    }

    @Test
    void createProject_ShouldFail_WhenNameIsBlankAfterTrim() {
        UUID workspaceId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);

        ProjectCreateRequest request = new ProjectCreateRequest(
                "   ", "Description", "Client",
                Status.TODO, Priority.MEDIUM, BigDecimal.TEN,
                Timestamp.from(Instant.parse("2026-03-14T10:00:00Z")),
                Timestamp.from(Instant.parse("2026-03-20T10:00:00Z")),
                null);

        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(workspaceId, request));

        verify(projectsRepository, never()).existsByNameIgnoreCaseAndWorkspaces_Id(any(), any());
        verify(workspaceRepository, never()).findById(any());
        verify(validate, never()).validateDateRange(any(), any());
    }

    @Test
    void updateProjectStatus_ShouldFail_WhenStatusIsMissing() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> projectService.updateProjectStatus(
                workspaceId, projectId, new ProjectStatusUpdateRequest(null, Priority.HIGH)));

        verify(currentUserService, never()).getAuthenticatedUser();
        verify(workspaceAccessPolicy, never()).requireManagePermission(any(), any());
        verify(projectsRepository, never()).findByIdAndWorkspaces_Id(any(), any());
    }

    private WorkspacesMembers membership(UUID workspaceId, User user, WorkspaceRole role) {
        Workspaces workspace = new Workspaces();
        workspace.setId(workspaceId);

        WorkspacesMembers membership = new WorkspacesMembers();
        membership.setWorkspaces(workspace);
        membership.setUser(user);
        membership.setRole(role);
        return membership;
    }

    private Projects project(UUID projectId, UUID workspaceId, User createdBy, User assignedTo) {
        Workspaces workspace = new Workspaces();
        workspace.setId(workspaceId);

        Projects project = new Projects();
        project.setId(projectId);
        project.setName("Project Alpha");
        project.setDescription("Description");
        project.setClient("Client");
        project.setStatus(Status.TODO);
        project.setPriority(Priority.HIGH);
        project.setBudget(BigDecimal.ONE);
        project.setStartDate(Timestamp.from(Instant.parse("2026-03-14T10:00:00Z")));
        project.setEndDate(Timestamp.from(Instant.parse("2026-03-20T10:00:00Z")));
        project.setCreatedBy(createdBy);
        project.setAssignedTo(assignedTo);
        project.setWorkspaces(workspace);
        return project;
    }
}


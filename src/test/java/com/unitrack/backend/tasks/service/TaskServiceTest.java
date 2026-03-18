package com.unitrack.backend.tasks.service;

import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.projects.repository.ProjectsRepository;
import com.unitrack.backend.tasks.dto.TaskAssignRequest;
import com.unitrack.backend.tasks.dto.TaskCreateRequest;
import com.unitrack.backend.tasks.dto.TaskResponse;
import com.unitrack.backend.tasks.dto.TaskStatusUpdateRequest;
import com.unitrack.backend.tasks.entity.Tasks;
import com.unitrack.backend.tasks.repository.TaskRepository;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.security.WorkspaceAccessPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectsRepository projectsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private WorkspaceAccessPolicy workspaceAccessPolicy;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTaskById_ShouldFail_WhenRequesterIsNotWorkspaceMember() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        doThrow(new AccessDeniedException("User is not a member of this workspace"))
                .when(workspaceAccessPolicy).requireMembership(workspaceId, requesterId);

        assertThrows(AccessDeniedException.class,
                () -> taskService.getTaskById(workspaceId, projectId, taskId));

        verify(taskRepository, never()).findByIdAndProject_IdAndProject_Workspaces_Id(any(), any(), any());
    }

    @Test
    void createTask_ShouldFail_WhenTitleIsBlankAfterTrim() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(projectsRepository.findByIdAndWorkspaces_Id(projectId, workspaceId))
                .thenReturn(Optional.of(project(projectId, workspaceId)));

        TaskCreateRequest request = new TaskCreateRequest(
                "   ",
                "Desc",
                Status.TODO,
                Priority.MEDIUM,
                Timestamp.from(Instant.parse("2026-03-20T10:00:00Z")),
                null);

        assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(workspaceId, projectId, request));

        verify(taskRepository, never()).save(any(Tasks.class));
    }

    @Test
    void createTask_ShouldMapCreatedByAndAssignedTo_WhenDataIsValid() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        User assignee = new User();
        assignee.setId(assigneeId);

        Projects project = project(projectId, workspaceId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(projectsRepository.findByIdAndWorkspaces_Id(projectId, workspaceId)).thenReturn(Optional.of(project));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Tasks.class))).thenAnswer(i -> {
            Tasks t = i.getArgument(0);
            t.setId(taskId);
            return t;
        });

        TaskCreateRequest request = new TaskCreateRequest(
                "Task Alpha",
                "Desc",
                Status.TODO,
                Priority.HIGH,
                Timestamp.from(Instant.parse("2026-03-20T10:00:00Z")),
                assigneeId);

        TaskResponse response = taskService.createTask(workspaceId, projectId, request);

        assertEquals(taskId, response.id());
        assertEquals(projectId, response.projectId());
        assertEquals(requesterId, response.createdById());
        assertEquals(assigneeId, response.assignedToId());

        ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(publisher).publishEvent(captor.capture());
        ActivityEvent event = captor.getValue();
        assertEquals(requesterId, event.getUserId());
        assertEquals(ActivityAction.CREATED, event.getAction());
        assertEquals(ActivityEntityType.TASKS, event.getEntityType());
        assertEquals(taskId, event.getEntityId());
    }

    @Test
    void assignTask_ShouldFail_WhenAssigneeIsNotWorkspaceMember() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(taskRepository.findByIdAndProject_IdAndProject_Workspaces_Id(taskId, projectId, workspaceId))
                .thenReturn(Optional.of(task(taskId, projectId, workspaceId, requester)));
        doThrow(new AccessDeniedException("User is not a member of this workspace"))
                .when(workspaceAccessPolicy).requireMembership(workspaceId, assigneeId);

        assertThrows(AccessDeniedException.class,
                () -> taskService.assignTask(workspaceId, projectId, taskId, new TaskAssignRequest(assigneeId)));

        verify(userRepository, never()).findById(assigneeId);
        verify(taskRepository, never()).save(any(Tasks.class));
    }

    @Test
    void unassignTask_ShouldBeIdempotent_WhenTaskIsAlreadyUnassigned() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(taskRepository.findByIdAndProject_IdAndProject_Workspaces_Id(taskId, projectId, workspaceId))
                .thenReturn(Optional.of(task(taskId, projectId, workspaceId, requester)));

        TaskResponse response = taskService.unassignTask(workspaceId, projectId, taskId);

        assertNull(response.assignedToId());
        verify(taskRepository, never()).save(any(Tasks.class));
    }

    @Test
    void updateTaskStatus_ShouldFail_WhenStatusIsMissing() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTaskStatus(
                        workspaceId,
                        projectId,
                        taskId,
                        new TaskStatusUpdateRequest(null, Priority.HIGH)));

        verify(currentUserService, never()).getAuthenticatedUser();
        verify(taskRepository, never()).findByIdAndProject_IdAndProject_Workspaces_Id(any(), any(), any());
    }

    @Test
    void getTasksByProject_ShouldReturnMappedList_WhenRequesterIsWorkspaceMember() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(projectsRepository.findByIdAndWorkspaces_Id(projectId, workspaceId))
                .thenReturn(Optional.of(project(projectId, workspaceId)));
        when(taskRepository.findByProject_IdAndProject_Workspaces_IdOrderByCreatedAtDesc(projectId, workspaceId))
                .thenReturn(List.of(task(taskId, projectId, workspaceId, requester)));

        List<TaskResponse> response = taskService.getTasksByProject(workspaceId, projectId);

        assertEquals(1, response.size());
        assertEquals(taskId, response.getFirst().id());
        assertEquals(projectId, response.getFirst().projectId());
    }

    @Test
    void deleteTask_ShouldDelete_WhenRequesterCanManage() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        Tasks task = task(taskId, projectId, workspaceId, requester);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(taskRepository.findByIdAndProject_IdAndProject_Workspaces_Id(taskId, projectId, workspaceId))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(workspaceId, projectId, taskId);

        verify(taskRepository).delete(task);
        ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(publisher).publishEvent(captor.capture());
        ActivityEvent event = captor.getValue();
        assertEquals(requesterId, event.getUserId());
        assertEquals(ActivityAction.DELETED, event.getAction());
        assertEquals(ActivityEntityType.TASKS, event.getEntityType());
        assertEquals(taskId, event.getEntityId());
    }

    @Test
    void updateTaskStatus_ShouldPublishUpdatedEvent_WhenRequestIsValid() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(taskRepository.findByIdAndProject_IdAndProject_Workspaces_Id(taskId, projectId, workspaceId))
                .thenReturn(Optional.of(task(taskId, projectId, workspaceId, requester)));
        when(taskRepository.save(any(Tasks.class))).thenAnswer(i -> i.getArgument(0));

        taskService.updateTaskStatus(
                workspaceId,
                projectId,
                taskId,
                new TaskStatusUpdateRequest(Status.IN_PROGRESS, Priority.HIGH));

        ArgumentCaptor<ActivityEvent> captor = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(publisher).publishEvent(captor.capture());
        ActivityEvent event = captor.getValue();
        assertEquals(requesterId, event.getUserId());
        assertEquals(ActivityAction.UPDATED, event.getAction());
        assertEquals(ActivityEntityType.TASKS, event.getEntityType());
        assertEquals(taskId, event.getEntityId());
    }

    private Projects project(UUID projectId, UUID workspaceId) {
        Workspaces workspace = new Workspaces();
        workspace.setId(workspaceId);

        Projects project = new Projects();
        project.setId(projectId);
        project.setWorkspaces(workspace);
        return project;
    }

    private Tasks task(UUID taskId, UUID projectId, UUID workspaceId, User createdBy) {
        Projects project = project(projectId, workspaceId);

        Tasks task = new Tasks();
        task.setId(taskId);
        task.setTitle("Task Alpha");
        task.setDescription("Desc");
        task.setStatus(Status.TODO);
        task.setPriority(Priority.MEDIUM);
        task.setDueDate(Timestamp.from(Instant.parse("2026-03-20T10:00:00Z")));
        task.setProject(project);
        task.setCreatedBy(createdBy);
        task.setAssignedTo(null);
        return task;
    }
}



package com.unitrack.backend.tasks.service;

import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.common.exception.NotFoundException;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.projects.repository.ProjectsRepository;
import com.unitrack.backend.tasks.dto.*;
import com.unitrack.backend.tasks.entity.Tasks;
import com.unitrack.backend.tasks.repository.TaskRepository;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.security.WorkspaceAccessPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectsRepository projectsRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final WorkspaceAccessPolicy workspaceAccessPolicy;

    @Transactional
    public TaskResponse createTask(UUID workspaceId, UUID projectId, TaskCreateRequest request) {
        if (request == null) {
            log.warn("TaskCreateRequest is null for project {} in workspace {}", projectId, workspaceId);
            throw new IllegalArgumentException("TaskCreateRequest cannot be null");
        }

        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Projects project = findProjectInWorkspace(projectId, workspaceId);
        String normalizedTitle = normalizeRequiredTitle(request.title());

        User assignedTo = resolveAssignee(workspaceId, request.assignedToId());

        Tasks task = new Tasks();
        task.setTitle(normalizedTitle);
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setProject(project);
        task.setCreatedBy(requester);
        task.setAssignedTo(assignedTo);

        Tasks savedTask = taskRepository.save(task);
        log.info("Created task with id {} in project {} and workspace {}", savedTask.getId(), projectId, workspaceId);
        return mapToResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(UUID workspaceId, UUID projectId, UUID taskId, TaskUpdateRequest request) {
        if (request == null) {
            log.warn("TaskUpdateRequest is null for task {} in project {} and workspace {}", taskId, projectId, workspaceId);
            throw new IllegalArgumentException("TaskUpdateRequest cannot be null");
        }

        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Tasks task = findTask(taskId, projectId, workspaceId);

        if (request.title() != null) task.setTitle(normalizeTitle(request.title()));
        if (request.description() != null) task.setDescription(request.description());
        if (request.status() != null) task.setStatus(request.status());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.assignedToId() != null) task.setAssignedTo(resolveAssignee(workspaceId, request.assignedToId()));

        Tasks savedTask = taskRepository.save(task);
        log.info("Updated task with id {} in project {} and workspace {}", savedTask.getId(), projectId, workspaceId);
        return mapToResponse(savedTask);
    }

    @Transactional
    public TaskResponse assignTask(UUID workspaceId, UUID projectId, UUID taskId, TaskAssignRequest request) {
        if (request == null || request.assignedToId() == null) {
            log.warn("TaskAssignRequest or assignedToId is null for task {} in project {} and workspace {}", taskId, projectId, workspaceId);
            throw new IllegalArgumentException("TaskAssignRequest and assignedToId cannot be null");
        }

        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Tasks task = findTask(taskId, projectId, workspaceId);
        task.setAssignedTo(resolveAssignee(workspaceId, request.assignedToId()));

        Tasks savedTask = taskRepository.save(task);
        log.info("Assigned task with id {} to user {} in project {} and workspace {}", savedTask.getId(), request.assignedToId(), projectId, workspaceId);
        return mapToResponse(savedTask);
    }

    @Transactional
    public TaskResponse unassignTask(UUID workspaceId, UUID projectId, UUID taskId) {
        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Tasks task = findTask(taskId, projectId, workspaceId);

        if (task.getAssignedTo() == null) {
            log.info("Task with id {} in project {} and workspace {} is already unassigned", taskId, projectId, workspaceId);
            return mapToResponse(task);
        }

        task.setAssignedTo(null);
        Tasks savedTask = taskRepository.save(task);
        log.info("Unassigned task with id {} in project {} and workspace {}", savedTask.getId(), projectId, workspaceId);
        return mapToResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTaskStatus(UUID workspaceId, UUID projectId, UUID taskId, TaskStatusUpdateRequest request) {
        if (request == null) {
            log.warn("TaskStatusUpdateRequest is null for task {} in project {} and workspace {}", taskId, projectId, workspaceId);
            throw new IllegalArgumentException("TaskStatusUpdateRequest cannot be null");
        }

        if (request.status() == null) {
            log.warn("Status is null in TaskStatusUpdateRequest for task {} in project {} and workspace {}", taskId, projectId, workspaceId);
            throw new IllegalArgumentException("Status cannot be null");
        }

        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Tasks task = findTask(taskId, projectId, workspaceId);
        task.setStatus(request.status());

        if (request.priority() != null) task.setPriority(request.priority());

        Tasks savedTask = taskRepository.save(task);
        log.info("Updated status of task with id {} to {} in project {} and workspace {}", savedTask.getId(), request.status(), projectId, workspaceId);
        return mapToResponse(savedTask);
    }

    @Transactional
    public void deleteTask(UUID workspaceId, UUID projectId, UUID taskId) {
        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspaceId, requester.getId());

        Tasks task = findTask(taskId, projectId, workspaceId);
        taskRepository.delete(task);
        log.info("Task '{}' deleted from project '{}' / workspace '{}'", taskId, projectId, workspaceId);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(UUID workspaceId, UUID projectId) {
        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireMembership(workspaceId, requester.getId());

        findProjectInWorkspace(projectId, workspaceId);

        return taskRepository
                .findByProject_IdAndProject_Workspaces_IdOrderByCreatedAtDesc(projectId, workspaceId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID workspaceId, UUID projectId, UUID taskId) {
        User requester = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireMembership(workspaceId, requester.getId());

        Tasks task = findTask(taskId, projectId, workspaceId);
        log.info("Task '{}' retrieved from project '{}' / workspace '{}'", taskId, projectId, workspaceId);
        return mapToResponse(task);
    }

    private Projects findProjectInWorkspace(UUID projectId, UUID workspaceId) {
        return projectsRepository.findByIdAndWorkspaces_Id(projectId, workspaceId)
                .orElseThrow(() -> {
                    log.warn("Project with id {} not found in workspace {}", projectId, workspaceId);
                    return new NotFoundException("Project not found in this workspace");
                });
    }

    private Tasks findTask(UUID taskId, UUID projectId, UUID workspaceId) {
        return taskRepository.findByIdAndProject_IdAndProject_Workspaces_Id(taskId, projectId, workspaceId)
                .orElseThrow(() -> {
                    log.warn("Task with id {} not found in project {} and workspace {}", taskId, projectId, workspaceId);
                    return new NotFoundException("Task not found in this project");
                });
    }

    private User resolveAssignee(UUID workspaceId, UUID assignedToId) {
        if (assignedToId == null) {
            return null;
        }

        workspaceAccessPolicy.requireMembership(workspaceId, assignedToId);
        return userRepository.findById(assignedToId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found for assignment in workspace {}", assignedToId, workspaceId);
                    return new NotFoundException("Assigned user not found");
                });
    }

    private String normalizeRequiredTitle(String raw) {
        if (raw == null) throw new IllegalArgumentException("Task title is required");
        return normalizeTitle(raw);
    }

    private String normalizeTitle(String raw) {
        String normalized = raw.trim();
        if (normalized.isBlank()) throw new IllegalArgumentException("Task title cannot be blank");
        return normalized;
    }

    private TaskResponse mapToResponse(Tasks task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getCreatedBy() != null ? task.getCreatedBy().getId() : null,
                task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

}


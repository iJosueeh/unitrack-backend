package com.unitrack.backend.tasks.controller;

import com.unitrack.backend.common.response.ApiResponse;
import com.unitrack.backend.tasks.dto.*;
import com.unitrack.backend.tasks.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TasksController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        TaskResponse response = taskService.createTask(workspaceId, projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TaskResponse>builder()
                        .success(true)
                        .message("Task created successfully")
                        .data(response)
                        .build()
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByProject(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId) {
        List<TaskResponse> tasks = taskService.getTasksByProject(workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.<List<TaskResponse>>builder()
                .success(true)
                .message("Tasks retrieved successfully")
                .data(tasks)
                .build());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {
        TaskResponse response = taskService.getTaskById(workspaceId, projectId, taskId);
        return ResponseEntity.ok(ApiResponse.<TaskResponse>builder()
                .success(true)
                .message("Task retrieved successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskUpdateRequest request) {
        TaskResponse response = taskService.updateTask(workspaceId, projectId, taskId, request);
        return ResponseEntity.ok(ApiResponse.<TaskResponse>builder()
                .success(true)
                .message("Task updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{taskId}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskAssignRequest request) {
        TaskResponse response = taskService.assignTask(workspaceId, projectId, taskId, request);
        return ResponseEntity.ok(ApiResponse.<TaskResponse>builder()
                .success(true)
                .message("Task assigned successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{taskId}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> unassignTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {
        TaskResponse response = taskService.unassignTask(workspaceId, projectId, taskId);
        return ResponseEntity.ok(ApiResponse.<TaskResponse>builder()
                .success(true)
                .message("Task unassigned successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskStatusUpdateRequest request) {
        TaskResponse response = taskService.updateTaskStatus(workspaceId, projectId, taskId, request);
        return ResponseEntity.ok(ApiResponse.<TaskResponse>builder()
                .success(true)
                .message("Task status updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {
        taskService.deleteTask(workspaceId, projectId, taskId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Task deleted successfully")
                .data(null)
                .build());
    }
}
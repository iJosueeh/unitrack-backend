package com.unitrack.backend.projects.controller;

import com.unitrack.backend.common.response.ApiResponse;
import com.unitrack.backend.projects.dto.*;
import com.unitrack.backend.projects.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
public class ProjectsController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody ProjectCreateRequest request) {

        ProjectResponse response = projectService.createProject(workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ProjectResponse>builder()
                        .success(true)
                        .message("Project created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectSummaryResponse>>> getProjectsByWorkspace(
            @PathVariable UUID workspaceId) {
        List<ProjectSummaryResponse> projects = projectService.getProjectsByWorkspace(workspaceId);
        return ResponseEntity.ok(ApiResponse.<List<ProjectSummaryResponse>>builder()
                .success(true)
                .message("Projects retrieved successfully")
                .data(projects)
                .build()
        );
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId) {
        ProjectResponse response = projectService.getProjectById(workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project retrieved successfully")
                .data(response)
                .build()
        );
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectUpdateRequest request) {
        ProjectResponse response = projectService.updateProject(workspaceId, projectId, request);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project updated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{projectId}/assign")
    public ResponseEntity<ApiResponse<ProjectResponse>> assignProjectMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectAssignRequest request) {
        ProjectResponse response = projectService.assignProjectMember(workspaceId, projectId, request);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project assigned successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{projectId}/assign")
    public ResponseEntity<ApiResponse<ProjectResponse>> unassignProjectMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId) {
        ProjectResponse response = projectService.unassignProjectMember(workspaceId, projectId);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project unassigned successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectStatusUpdateRequest request) {
        ProjectResponse response = projectService.updateProjectStatus(workspaceId, projectId, request);
        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true)
                .message("Project status updated successfully")
                .data(response)
                .build());
    }

}

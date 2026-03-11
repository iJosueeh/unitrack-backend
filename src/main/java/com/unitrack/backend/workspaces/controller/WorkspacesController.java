package com.unitrack.backend.workspaces.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unitrack.backend.common.response.ApiResponse;
import com.unitrack.backend.workspaces.dto.WorkspaceCreateRequest;
import com.unitrack.backend.workspaces.dto.WorkspaceResponse;
import com.unitrack.backend.workspaces.service.WorkspaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspacesController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> createWorkspace(
            @Valid @RequestBody WorkspaceCreateRequest request) {
        WorkspaceResponse workspaceResponse = workspaceService.createWorkspace(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<WorkspaceResponse>builder()
                        .success(true)
                        .message("Workspace created successfully")
                        .data(workspaceResponse)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getMyWorkspaces() {
        List<WorkspaceResponse> workspaces = workspaceService.getMyWorkspaces();
        return ResponseEntity.ok(ApiResponse.<List<WorkspaceResponse>>builder()
                .success(true)
                .message("Workspaces retrieved successfully")
                .data(workspaces)
                .build());
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getWorkspaceById(@PathVariable UUID workspaceId) {
        WorkspaceResponse workspace = workspaceService.getWorkspaceById(workspaceId);
        return ResponseEntity.ok(ApiResponse.<WorkspaceResponse>builder()
                .success(true)
                .message("Workspace retrieved successfully")
                .data(workspace)
                .build());
    }

}

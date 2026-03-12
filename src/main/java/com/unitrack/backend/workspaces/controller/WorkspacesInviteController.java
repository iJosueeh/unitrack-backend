package com.unitrack.backend.workspaces.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unitrack.backend.common.response.ApiResponse;
import com.unitrack.backend.workspaces.dto.AcceptInviteRequest;
import com.unitrack.backend.workspaces.dto.ActiveWorkspaceInviteResponse;
import com.unitrack.backend.workspaces.dto.CreatedInviteRequest;
import com.unitrack.backend.workspaces.dto.CreatedInviteResponse;
import com.unitrack.backend.workspaces.service.WorkspaceInviteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/workspaces/invites")
@RequiredArgsConstructor
public class WorkspacesInviteController {

    private final WorkspaceInviteService workspaceInviteService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreatedInviteResponse>> createInvite(
            @Valid @RequestBody CreatedInviteRequest request) {
        CreatedInviteResponse inviteResponse = workspaceInviteService.createInvite(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CreatedInviteResponse>builder()
                        .success(true)
                        .message("Invite created successfully")
                        .data(inviteResponse)
                        .build());
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvite(
                        @Valid @RequestBody AcceptInviteRequest request) {
                workspaceInviteService.acceptInvite(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Invite accepted successfully")
                .data(null)
                .build());
    }

    @GetMapping("/workspace/{workspaceId}/active")
    public ResponseEntity<ApiResponse<ActiveWorkspaceInviteResponse>> getActiveInvite(@PathVariable UUID workspaceId) {
        ActiveWorkspaceInviteResponse response = workspaceInviteService.getActiveInvite(workspaceId);
        return ResponseEntity.ok(ApiResponse.<ActiveWorkspaceInviteResponse>builder()
                .success(true)
                .message("Active invite retrieved successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{inviteId}")
    public ResponseEntity<ApiResponse<Void>> deactivateInvite(@PathVariable UUID inviteId) {
        workspaceInviteService.deactivateInvite(inviteId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Invite deactivated successfully")
                .data(null)
                .build());
    }
}

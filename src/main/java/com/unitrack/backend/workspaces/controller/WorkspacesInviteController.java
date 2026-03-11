package com.unitrack.backend.workspaces.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unitrack.backend.common.response.ApiResponse;
import com.unitrack.backend.workspaces.dto.AcceptInviteRequest;
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
}

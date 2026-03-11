package com.unitrack.backend.workspaces.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unitrack.backend.common.response.ApiResponse;
import com.unitrack.backend.workspaces.dto.WorkspaceMemberResponse;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.service.WorkspaceMemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspacesMembersController {

    private final WorkspaceMemberService workspaceMemberService;

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<ApiResponse<List<WorkspaceMemberResponse>>> getMembers(@PathVariable UUID workspaceId) {
        List<WorkspaceMemberResponse> members = workspaceMemberService.getMembers(workspaceId);
        return ResponseEntity.ok(ApiResponse.<List<WorkspaceMemberResponse>>builder()
                .success(true)
                .message("Members retrieved successfully")
                .data(members)
                .build());
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable UUID workspaceId, @PathVariable UUID userId) {
        workspaceMemberService.removeMember(workspaceId, userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Member removed successfully")
                .data(null)
                .build());
    }

    @PatchMapping("/{workspaceId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(@PathVariable UUID workspaceId,
            @PathVariable UUID userId, @RequestParam WorkspaceRole role) {
        workspaceMemberService.updateMemberRole(workspaceId, userId, role);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Member role updated successfully")
                .data(null)
                .build());
    }

}

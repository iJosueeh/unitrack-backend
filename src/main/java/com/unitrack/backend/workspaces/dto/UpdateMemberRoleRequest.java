package com.unitrack.backend.workspaces.dto;

import com.unitrack.backend.workspaces.enums.WorkspaceRole;

import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
    @NotNull(message = "Role is required")
    WorkspaceRole role
) {}
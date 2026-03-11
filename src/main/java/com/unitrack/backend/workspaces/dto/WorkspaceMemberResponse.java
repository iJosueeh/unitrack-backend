package com.unitrack.backend.workspaces.dto;

import java.sql.Timestamp;
import java.util.UUID;

import com.unitrack.backend.workspaces.enums.WorkspaceRole;

public record WorkspaceMemberResponse(
    UUID memberId,
    UUID userId,
    String firstName,
    String lastName,
    String email,
    WorkspaceRole role,
    Timestamp joinedAt
) {}

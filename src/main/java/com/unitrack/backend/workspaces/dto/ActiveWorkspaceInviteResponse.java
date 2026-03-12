package com.unitrack.backend.workspaces.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActiveWorkspaceInviteResponse(
    UUID inviteId,
    LocalDateTime expiresAt,
    Integer maxUses,
    Integer usedCount,
    Boolean isActive
) {}
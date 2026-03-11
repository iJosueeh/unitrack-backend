package com.unitrack.backend.workspaces.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreatedInviteResponse(
    UUID inviteId,
    String code,
    LocalDateTime expiresAt
) {}

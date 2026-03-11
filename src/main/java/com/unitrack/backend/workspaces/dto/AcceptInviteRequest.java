package com.unitrack.backend.workspaces.dto;

import jakarta.validation.constraints.NotBlank;

public record AcceptInviteRequest(
    @NotBlank(message = "Invite code must not be blank")
    String code
) {}

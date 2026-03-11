package com.unitrack.backend.workspaces.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatedInviteRequest {

    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;

    @NotNull(message = "ID of the user creating the invite is required")
    private UUID createdById;

}

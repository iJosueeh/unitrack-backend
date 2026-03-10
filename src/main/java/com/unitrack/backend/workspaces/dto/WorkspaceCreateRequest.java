package com.unitrack.backend.workspaces.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceCreateRequest {
    
    @NotBlank(message = "Workspace name is required")
    @Size(min = 2, max = 120, message = "Workspace name must have between 2 and 120 characters")
    private String name;

    @NotNull(message = "Owner ID is required")
    private UUID ownerId;

}

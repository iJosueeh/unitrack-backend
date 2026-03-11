package com.unitrack.backend.workspaces.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @NotNull(message = "Limit of members is required")
    @Min(value = 1, message = "Limit of members must be a positive integer")
    @Max(value = 1000, message = "Limit of members must be less than or equal to 1000")
    private Integer limitMembers;

}

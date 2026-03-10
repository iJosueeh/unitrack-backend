package com.unitrack.backend.workspaces.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class WorkspaceResponse {

    private UUID id;
    private String name;
    private UUID ownerId;
    private Integer membersCount;
    private Integer projectsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

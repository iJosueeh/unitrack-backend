package com.unitrack.backend.workspaces.service;

import org.springframework.stereotype.Service;

import com.unitrack.backend.workspaces.dto.WorkspaceResponse;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    

    private WorkspaceResponse mapToResponse(Workspaces body) {
        return WorkspaceResponse.builder()
                .id(body.getId())
                .name(body.getName())
                .ownerId(body.getOwnerId().getId())
                .membersCount(body.getMembers().size())
                .projectsCount(body.getProjects().size())
                .createdAt(body.getCreatedAt())
                .updatedAt(body.getUpdatedAt())
                .build();
    }

}

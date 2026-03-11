package com.unitrack.backend.workspaces.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.dto.WorkspaceCreateRequest;
import com.unitrack.backend.workspaces.dto.WorkspaceResponse;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private static final int MAX_WORKSPACES_PER_USER = 1;

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberService WorkspaceMemberService;

    @Transactional
    public Workspaces createWorkspaces(WorkspaceCreateRequest request) {
        if (request == null) {
            log.error("WorkspaceCreateRequest is null. Cannot create workspace.");
            throw new IllegalArgumentException("WorkspaceCreateRequest cannot be null");
        }

        Long existingCount = workspaceRepository.countByOwnerId(request.getOwnerId());
        if (existingCount >= MAX_WORKSPACES_PER_USER) {
            log.warn("User {} already reached the workspace limit ({})", request.getOwnerId(), MAX_WORKSPACES_PER_USER);
            throw new IllegalArgumentException("User has reached the maximum number of workspaces allowed");
        }

        if (workspaceRepository.findByName(request.getName()) != null) {
            log.warn("Workspace with name {} already exists", request.getName());
            throw new IllegalArgumentException("Workspace name already exists");
        }

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> {
                    log.warn("Owner con id {} no encontrado", request.getOwnerId());
                    throw new RuntimeException("Owner user not found");
                });

        Workspaces workspaces = new Workspaces();
        workspaces.setName(request.getName());
        workspaces.setOwnerId(owner);
        workspaces.setLimitMembers(request.getLimitMembers());

        Workspaces savedWorkspace = workspaceRepository.save(workspaces);
        WorkspaceMemberService.createOwnerMembership(savedWorkspace, owner);
        log.info("Workspace created with name: {}", savedWorkspace.getName());
        return savedWorkspace;
    }

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

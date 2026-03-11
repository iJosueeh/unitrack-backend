package com.unitrack.backend.workspaces.service;

import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.workspaces.dto.WorkspaceCreateRequest;
import com.unitrack.backend.workspaces.dto.WorkspaceResponse;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;
import com.unitrack.backend.workspaces.repository.WorkspaceSummaryView;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private static final int MAX_WORKSPACES_PER_USER = 1;

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberService workspaceMemberService;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request) {
        User owner = currentUserService.getAuthenticatedUser();

        long existingCount = workspaceRepository.countByOwnerId_Id(owner.getId());
        if (existingCount >= MAX_WORKSPACES_PER_USER) {
            log.warn("User {} reached the workspace limit ({})", owner.getId(), MAX_WORKSPACES_PER_USER);
            throw new IllegalArgumentException("User has reached the maximum number of workspaces allowed");
        }

        if (workspaceRepository.findByName(request.getName()) != null) {
            log.warn("Workspace with name '{}' already exists", request.getName());
            throw new IllegalArgumentException("Workspace name already exists");
        }

        Workspaces workspace = new Workspaces();
        workspace.setName(request.getName());
        workspace.setOwnerId(owner);
        workspace.setLimitMembers(request.getLimitMembers());

        Workspaces saved = workspaceRepository.save(workspace);
        workspaceMemberService.createOwnerMembership(saved, owner);

        publisher.publishEvent(new ActivityEvent(
                owner.getId(),
                ActivityAction.CREATED,
                ActivityEntityType.WORKSPACE,
                saved.getId()));

        log.info("Workspace '{}' created by user {}", saved.getName(), owner.getId());
        WorkspaceSummaryView summary = workspaceRepository.findWorkspaceSummaryByIdForUser(saved.getId(), owner.getId())
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
        return mapToResponse(summary);
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID workspaceId) {
        User user = currentUserService.getAuthenticatedUser();

        WorkspaceSummaryView summary = workspaceRepository.findWorkspaceSummaryByIdForUser(workspaceId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("User is not a member of this workspace"));

        return mapToResponse(summary);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getMyWorkspaces() {
        User user = currentUserService.getAuthenticatedUser();

        return workspaceRepository.findWorkspaceSummariesByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private WorkspaceResponse mapToResponse(WorkspaceSummaryView body) {
        return WorkspaceResponse.builder()
                .id(body.getId())
                .name(body.getName())
                .ownerId(body.getOwnerId())
                .membersCount(Math.toIntExact(body.getMembersCount()))
                .projectsCount(Math.toIntExact(body.getProjectsCount()))
                .createdAt(body.getCreatedAt())
                .updatedAt(body.getUpdatedAt())
                .build();
    }

}

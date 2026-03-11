package com.unitrack.backend.workspaces.service;

import java.sql.Timestamp;
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
import com.unitrack.backend.workspaces.dto.WorkspaceMemberResponse;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.entity.WorkspacesMembers;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;
import com.unitrack.backend.workspaces.repository.WorkspacesMembersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspacesMembersRepository workspacesMembersRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher publisher;

    public void createOwnerMembership(Workspaces workspace, User owner) {
        WorkspacesMembers membership = new WorkspacesMembers();
        membership.setWorkspaces(workspace);
        membership.setUser(owner);
        membership.setRole(WorkspaceRole.OWNER);
        membership.setJoinedAt(new Timestamp(System.currentTimeMillis()));
        workspacesMembersRepository.save(membership);
        log.info("Owner membership created for user {} in workspace {}", owner.getId(), workspace.getId());
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getMembers(UUID workspaceId) {
        User requester = currentUserService.getAuthenticatedUser();

        if (!workspacesMembersRepository.existsByWorkspaces_IdAndUser_Id(workspaceId, requester.getId())) {
            throw new AccessDeniedException("User is not a member of this workspace");
        }

        return workspacesMembersRepository.findByWorkspaces_Id(workspaceId)
                .stream()
                .map(m -> new WorkspaceMemberResponse(
                        m.getId(),
                        m.getUser().getId(),
                        m.getUser().getFirstName(),
                        m.getUser().getLastName(),
                        m.getUser().getEmail(),
                        m.getRole(),
                        m.getJoinedAt()))
                .toList();
    }

    @Transactional
    public void removeMember(UUID workspaceId, UUID targetUserId) {
        User requester = currentUserService.getAuthenticatedUser();
        validateCanManageMembers(workspaceId, requester.getId());

        WorkspacesMembers target = workspacesMembersRepository
                .findByWorkspaces_IdAndUser_Id(workspaceId, targetUserId);

        if (target == null) {
            throw new IllegalArgumentException("User is not a member of this workspace");
        }
        if (target.getRole() == WorkspaceRole.OWNER) {
            throw new IllegalArgumentException("Cannot remove the workspace owner");
        }

        workspacesMembersRepository.delete(target);
        publisher.publishEvent(new ActivityEvent(
                requester.getId(),
                ActivityAction.DELETED,
                ActivityEntityType.WORKSPACE_MEMBERS,
                target.getId()));
        log.info("User {} removed member {} from workspace {}", requester.getId(), targetUserId, workspaceId);
    }

    @Transactional
    public void updateMemberRole(UUID workspaceId, UUID targetUserId, WorkspaceRole newRole) {
        User requester = currentUserService.getAuthenticatedUser();
        validateIsOwner(workspaceId, requester.getId());

        if (newRole == WorkspaceRole.OWNER) {
            throw new IllegalArgumentException("Cannot assign the OWNER role through this operation");
        }

        WorkspacesMembers membership = workspacesMembersRepository
                .findByWorkspaces_IdAndUser_Id(workspaceId, targetUserId);

        if (membership == null) {
            throw new IllegalArgumentException("User is not a member of this workspace");
        }
        if (membership.getRole() == WorkspaceRole.OWNER) {
            throw new IllegalArgumentException("Cannot change the role of the workspace owner");
        }

        membership.setRole(newRole);
        workspacesMembersRepository.save(membership);
        publisher.publishEvent(new ActivityEvent(
                requester.getId(),
                ActivityAction.UPDATED,
                ActivityEntityType.WORKSPACE_MEMBERS,
                membership.getId()));
        log.info("User {} updated role of {} to {} in workspace {}", requester.getId(), targetUserId, newRole, workspaceId);
    }

    private void validateCanManageMembers(UUID workspaceId, UUID userId) {
        WorkspacesMembers membership = workspacesMembersRepository
                .findByWorkspaces_IdAndUser_Id(workspaceId, userId);
        if (membership == null) {
            throw new AccessDeniedException("User is not a member of this workspace");
        }
        if (membership.getRole() != WorkspaceRole.OWNER && membership.getRole() != WorkspaceRole.ADMIN) {
            throw new AccessDeniedException("Only OWNER or ADMIN can manage workspace members");
        }
    }

    private void validateIsOwner(UUID workspaceId, UUID userId) {
        WorkspacesMembers membership = workspacesMembersRepository
                .findByWorkspaces_IdAndUser_Id(workspaceId, userId);
        if (membership == null || membership.getRole() != WorkspaceRole.OWNER) {
            throw new AccessDeniedException("Only the workspace OWNER can change member roles");
        }
    }

    @SuppressWarnings("unused")
    private Workspaces loadWorkspace(UUID workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));
    }

}

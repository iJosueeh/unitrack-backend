package com.unitrack.backend.workspaces.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.workspaces.dto.AcceptInviteRequest;
import com.unitrack.backend.workspaces.dto.ActiveWorkspaceInviteResponse;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.dto.CreatedInviteRequest;
import com.unitrack.backend.workspaces.dto.CreatedInviteResponse;
import com.unitrack.backend.workspaces.entity.WorkspaceInvite;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.entity.WorkspacesMembers;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.repository.WorkspaceInviteRepository;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;
import com.unitrack.backend.workspaces.repository.WorkspacesMembersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceInviteService {

    private final WorkspaceRepository workspaceRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final WorkspaceInviteRepository workspaceInviteRepository;
    private final WorkspacesMembersRepository workspacesMembersRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public CreatedInviteResponse createInvite(CreatedInviteRequest request) {
        if (request == null) {
            log.error("CreatedInviteRequest is null. Cannot create invite.");
            throw new IllegalArgumentException("CreatedInviteRequest cannot be null");
        }

        if (request.getWorkspaceId() == null) {
            log.error("Workspace ID is null in the request");
            throw new IllegalArgumentException("Workspace ID is required");
        }

        Workspaces workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> {
                    log.error("Workspace with ID {} not found", request.getWorkspaceId());
                    return new IllegalArgumentException("Workspace not found");
                });

        User user = currentUserService.getAuthenticatedUser();

        validateCanManageInvites(workspace, user.getId());

        List<WorkspaceInvite> activeInvites = workspaceInviteRepository
            .findByWorkspaces_IdAndIsActiveTrue(workspace.getId());
        if (!activeInvites.isEmpty()) {
            activeInvites.forEach(invite -> invite.setIsActive(false));
            workspaceInviteRepository.saveAll(activeInvites);
            log.info("Deactivated {} existing active invite(s) for workspace {}", activeInvites.size(),
                workspace.getId());
        }

        String rawCode = UUID.randomUUID().toString().replace("-", "");
        String codeHash = hashInviteCode(rawCode);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        WorkspaceInvite invite = new WorkspaceInvite();
        invite.setWorkspaces(workspace);
        invite.setCreatedBy(user);
        invite.setCodeHash(codeHash);
        invite.setExpiresAt(expiresAt);
        invite.setIsActive(true);
        invite.setMaxUses(1);
        invite.setUsedCount(0);

        WorkspaceInvite savedInvite;
        try {
            savedInvite = workspaceInviteRepository.save(invite);
        } catch (DataIntegrityViolationException e) {
            log.warn("Workspace {} already has an active invite due to concurrent request", workspace.getId());
            throw new IllegalArgumentException("Workspace already has an active invite");
        }
        log.info("Workspace invite created with ID: {}", savedInvite.getId());

        publisher.publishEvent(new ActivityEvent(
                user.getId(),
                ActivityAction.CREATED,
                ActivityEntityType.WORKSPACE_INVITE,
                savedInvite.getId()));
        return new CreatedInviteResponse(
                savedInvite.getId(),
                rawCode,
                expiresAt);
    }

    @Transactional
    public void acceptInvite(AcceptInviteRequest request) {
        if (request == null) {
            log.error("AcceptInviteRequest is null");
            throw new IllegalArgumentException("AcceptInviteRequest cannot be null");
        }

        if (request.code() == null || request.code().isBlank()) {
            log.error("Raw invite code is null or blank");
            throw new IllegalArgumentException("Invite code is required");
        }

        User authenticatedUser = currentUserService.getAuthenticatedUser();
        UUID userId = authenticatedUser.getId();

        String normalizedCode = normalizeInviteCode(request.code());
        WorkspaceInvite invite = findValidInviteByCode(normalizedCode);
        Workspaces workspace = invite.getWorkspaces();

        if (workspacesMembersRepository.existsByWorkspaces_IdAndUser_Id(workspace.getId(), userId)) {
            log.warn("User with ID {} is already a member of workspace {}", userId, workspace.getId());
            throw new IllegalArgumentException("User is already a member of the workspace");
        }

        validateMemberLimit(workspace);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", userId);
                    return new IllegalArgumentException("User not found");
                });

        WorkspacesMembers membership = new WorkspacesMembers();
        membership.setWorkspaces(workspace);
        membership.setUser(user);
        membership.setRole(WorkspaceRole.USER);
        membership.setJoinedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        try {
            workspacesMembersRepository.save(membership);
            publisher.publishEvent(new ActivityEvent(
                    userId,
                    ActivityAction.CREATED,
                    ActivityEntityType.WORKSPACE_MEMBERS,
                    membership.getId()));
        } catch (DataIntegrityViolationException e) {
            log.warn("User {} attempted to join workspace {} concurrently", userId, workspace.getId());
            throw new IllegalArgumentException("User is already a member of the workspace");
        }

        invite.setUsedCount(invite.getUsedCount() + 1);
        invite.setIsActive(false);

        workspaceInviteRepository.save(invite);
        log.info("User with ID {} accepted invite and joined workspace {}", userId, workspace.getId());
    }

    @Transactional(readOnly = true)
    public ActiveWorkspaceInviteResponse getActiveInvite(UUID workspaceId) {
        if (workspaceId == null) {
            throw new IllegalArgumentException("Workspace ID is required");
        }

        User user = currentUserService.getAuthenticatedUser();

        Workspaces workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        validateCanManageInvites(workspace, user.getId());

        WorkspaceInvite activeInvite = workspaceInviteRepository.findByWorkspaces_IdAndIsActiveTrue(workspaceId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Workspace has no active invite"));

        return new ActiveWorkspaceInviteResponse(
                activeInvite.getId(),
                activeInvite.getExpiresAt(),
                activeInvite.getMaxUses(),
                activeInvite.getUsedCount(),
                activeInvite.getIsActive());
    }

    @Transactional
    public void deactivateInvite(UUID inviteId) {
        if (inviteId == null) {
            throw new IllegalArgumentException("Invite ID is required");
        }

        User user = currentUserService.getAuthenticatedUser();

        WorkspaceInvite invite = workspaceInviteRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found"));

        validateCanManageInvites(invite.getWorkspaces(), user.getId());

        if (!Boolean.TRUE.equals(invite.getIsActive())) {
            throw new IllegalArgumentException("Invite is already inactive");
        }

        invite.setIsActive(false);
        workspaceInviteRepository.save(invite);

        publisher.publishEvent(new ActivityEvent(
                user.getId(),
                ActivityAction.UPDATED,
                ActivityEntityType.WORKSPACE_INVITE,
                invite.getId()));
    }

    private String hashInviteCode(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error occurred while hashing the value", e);
            throw new IllegalStateException("Error occurred while hashing the value", e);
        }
    }

    private String normalizeInviteCode(String rawCode) {
        return rawCode.trim().replace("-", "").toLowerCase();
    }

    private void validateCanManageInvites(Workspaces workspace, UUID userId) {
        UUID workspaceId = workspace.getId();
        if (workspace.getOwnerId().getId().equals(userId)) {
            return;
        }

        WorkspacesMembers membership = workspacesMembersRepository
                .findByWorkspaces_IdAndUser_Id(workspaceId, userId);

        if (membership == null) {
            log.error("User with ID {} is not a member of workspace {}", userId, workspaceId);
            throw new AccessDeniedException("User is not a member of the workspace");
        }

        if (membership.getRole() != WorkspaceRole.OWNER && membership.getRole() != WorkspaceRole.ADMIN) {
            throw new AccessDeniedException("User is not allowed to manage invites");
        }
    }

    private WorkspaceInvite findValidInviteByCode(String rawCode) {
        String codeHash = hashInviteCode(rawCode);

        WorkspaceInvite invite = workspaceInviteRepository.findByCodeHash(codeHash)
                .orElseThrow(() -> {
                    log.warn("Invalid invite code provided");
                    return new IllegalArgumentException("Invalid invite code");
                });

        if (!Boolean.TRUE.equals(invite.getIsActive())) {
            log.warn("Invite is not active");
            throw new IllegalArgumentException("Invite is not active");
        }

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Invite has expired");
            throw new IllegalArgumentException("Invite has expired");
        }

        if (invite.getUsedCount() >= invite.getMaxUses()) {
            log.warn("Invite has reached its maximum uses");
            throw new IllegalArgumentException("Invite has reached its maximum uses");
        }

        return invite;
    }

    private void validateMemberLimit(Workspaces workspace) {
        Integer limit = workspace.getLimitMembers();
        if (limit == null) {
            log.info("Workspace {} has no member limit", workspace.getId());
            return;
        }

        long currentMembers = workspacesMembersRepository.countByWorkspaces_Id(workspace.getId());
        if (currentMembers >= limit) {
            log.warn("Workspace {} has reached its member limit", workspace.getId());
            throw new IllegalArgumentException("Workspace has reached its member limit");
        }
    }
}

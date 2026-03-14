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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unitrack.backend.activity.enums.ActivityAction;
import com.unitrack.backend.activity.enums.ActivityEntityType;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.common.exception.ConflictException;
import com.unitrack.backend.common.exception.NotFoundException;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.dto.AcceptInviteRequest;
import com.unitrack.backend.workspaces.dto.ActiveWorkspaceInviteResponse;
import com.unitrack.backend.workspaces.dto.CreatedInviteRequest;
import com.unitrack.backend.workspaces.dto.CreatedInviteResponse;
import com.unitrack.backend.workspaces.entity.WorkspaceInvite;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.entity.WorkspacesMembers;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.repository.WorkspaceInviteRepository;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;
import com.unitrack.backend.workspaces.repository.WorkspacesMembersRepository;
import com.unitrack.backend.workspaces.security.WorkspaceAccessPolicy;

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
    private final WorkspaceAccessPolicy workspaceAccessPolicy;

    @Transactional
    public CreatedInviteResponse createInvite(CreatedInviteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreatedInviteRequest cannot be null");
        }
        if (request.getWorkspaceId() == null) {
            throw new IllegalArgumentException("Workspace ID is required");
        }

        Workspaces workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        User user = currentUserService.getAuthenticatedUser();
        workspaceAccessPolicy.requireManagePermission(workspace.getId(), user.getId());

        List<WorkspaceInvite> activeInvites = workspaceInviteRepository
                .findByWorkspaces_IdAndIsActiveTrue(workspace.getId());
        if (!activeInvites.isEmpty()) {
            activeInvites.forEach(invite -> invite.setIsActive(false));
            workspaceInviteRepository.saveAll(activeInvites);
            log.info("Deactivated {} existing active invite(s) for workspace {}", activeInvites.size(), workspace.getId());
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
            throw new ConflictException("Workspace already has an active invite");
        }

        publisher.publishEvent(new ActivityEvent(
                user.getId(), ActivityAction.CREATED, ActivityEntityType.WORKSPACE_INVITE, savedInvite.getId()));
        log.info("Workspace invite created with ID: {}", savedInvite.getId());
        return new CreatedInviteResponse(savedInvite.getId(), rawCode, expiresAt);
    }

    @Transactional
    public void acceptInvite(AcceptInviteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AcceptInviteRequest cannot be null");
        }
        if (request.code() == null || request.code().isBlank()) {
            throw new IllegalArgumentException("Invite code is required");
        }

        User authenticatedUser = currentUserService.getAuthenticatedUser();
        UUID userId = authenticatedUser.getId();

        String normalizedCode = normalizeInviteCode(request.code());
        WorkspaceInvite invite = findValidInviteByCode(normalizedCode);
        Workspaces workspace = invite.getWorkspaces();

        if (workspacesMembersRepository.existsByWorkspaces_IdAndUser_Id(workspace.getId(), userId)) {
            log.warn("User {} is already a member of workspace {}", userId, workspace.getId());
            throw new ConflictException("User is already a member of the workspace");
        }

        validateMemberLimit(workspace);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        WorkspacesMembers membership = new WorkspacesMembers();
        membership.setWorkspaces(workspace);
        membership.setUser(user);
        membership.setRole(WorkspaceRole.USER);
        membership.setJoinedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        try {
            workspacesMembersRepository.save(membership);
            publisher.publishEvent(new ActivityEvent(
                    userId, ActivityAction.CREATED, ActivityEntityType.WORKSPACE_MEMBERS, membership.getId()));
        } catch (DataIntegrityViolationException e) {
            log.warn("User {} attempted to join workspace {} concurrently", userId, workspace.getId());
            throw new ConflictException("User is already a member of the workspace");
        }

        invite.setUsedCount(invite.getUsedCount() + 1);
        invite.setIsActive(false);
        workspaceInviteRepository.save(invite);
        log.info("User {} accepted invite and joined workspace {}", userId, workspace.getId());
    }

    @Transactional(readOnly = true)
    public ActiveWorkspaceInviteResponse getActiveInvite(UUID workspaceId) {
        if (workspaceId == null) {
            throw new IllegalArgumentException("Workspace ID is required");
        }

        User user = currentUserService.getAuthenticatedUser();
        Workspaces workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        workspaceAccessPolicy.requireManagePermission(workspace.getId(), user.getId());

        WorkspaceInvite activeInvite = workspaceInviteRepository.findByWorkspaces_IdAndIsActiveTrue(workspaceId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Workspace has no active invite"));

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
                .orElseThrow(() -> new NotFoundException("Invite not found"));

        workspaceAccessPolicy.requireManagePermission(invite.getWorkspaces().getId(), user.getId());

        if (!Boolean.TRUE.equals(invite.getIsActive())) {
            throw new IllegalArgumentException("Invite is already inactive");
        }

        invite.setIsActive(false);
        workspaceInviteRepository.save(invite);
        publisher.publishEvent(new ActivityEvent(
                user.getId(), ActivityAction.UPDATED, ActivityEntityType.WORKSPACE_INVITE, invite.getId()));
    }

    private String hashInviteCode(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error hashing invite code", e);
            throw new IllegalStateException("Error occurred while hashing the value", e);
        }
    }

    private String normalizeInviteCode(String rawCode) {
        return rawCode.trim().replace("-", "").toLowerCase();
    }

    private WorkspaceInvite findValidInviteByCode(String rawCode) {
        String codeHash = hashInviteCode(rawCode);
        WorkspaceInvite invite = workspaceInviteRepository.findByCodeHash(codeHash)
                .orElseThrow(() -> {
                    log.warn("Invalid invite code provided");
                    return new IllegalArgumentException("Invalid invite code");
                });

        if (!Boolean.TRUE.equals(invite.getIsActive())) {
            throw new IllegalArgumentException("Invite is not active");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invite has expired");
        }
        if (invite.getUsedCount() >= invite.getMaxUses()) {
            throw new IllegalArgumentException("Invite has reached its maximum uses");
        }
        return invite;
    }

    private void validateMemberLimit(Workspaces workspace) {
        Integer limit = workspace.getLimitMembers();
        if (limit == null) return;

        long currentMembers = workspacesMembersRepository.countByWorkspaces_Id(workspace.getId());
        if (currentMembers >= limit) {
            log.warn("Workspace {} has reached its member limit", workspace.getId());
            throw new IllegalArgumentException("Workspace has reached its member limit");
        }
    }
}

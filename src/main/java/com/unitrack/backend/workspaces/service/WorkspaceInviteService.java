package com.unitrack.backend.workspaces.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;
    private final WorkspaceInviteRepository workspaceInviteRepository;
    private final WorkspacesMembersRepository workspacesMembersRepository;

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

        if (request.getCreatedById() == null) {
            log.error("CreatedBy ID is null in the request");
            throw new IllegalArgumentException("CreatedBy ID is required");
        }

        Workspaces workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> {
                    log.error("Workspace with ID {} not found", request.getWorkspaceId());
                    return new IllegalArgumentException("Workspace not found");
                });

        User user = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", request.getCreatedById());
                    return new IllegalArgumentException("User not found");
                });

        validateCanManageInvites(workspace.getId(), user.getId());

        if (!workspaceInviteRepository.findByWorkspaces_IdAndIsActiveTrue(workspace.getId()).isEmpty()) {
            log.warn("Workspace {} already has an active invite", workspace.getId());
            throw new IllegalArgumentException("Workspace already has an active invite");
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

        WorkspaceInvite savedInvite = workspaceInviteRepository.save(invite);
        log.info("Workspace invite created with ID: {}", savedInvite.getId());

        return new CreatedInviteResponse(
                savedInvite.getId(),
                rawCode,
                expiresAt);
    }

    @Transactional
    public void acceptInvite(String rawCode, UUID userId) {
        if (rawCode == null || rawCode.isBlank()) {
            log.error("Raw invite code is null or blank");
            throw new IllegalArgumentException("Invite code is required");
        }

        if (userId == null) {
            log.error("User ID is null");
            throw new IllegalArgumentException("User ID is required");
        }

        WorkspaceInvite invite = findValidInviteByCode(rawCode);
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
        workspacesMembersRepository.save(membership);

        invite.setUsedCount(invite.getUsedCount() + 1);
        if (invite.getUsedCount() >= invite.getMaxUses()) {
            invite.setIsActive(false);
        }
        workspaceInviteRepository.save(invite);
        log.info("User with ID {} accepted invite and joined workspace {}", userId, workspace.getId());
    }

    private String hashInviteCode(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error occurred while hashing the value", e);
            throw new RuntimeException("Error occurred while hashing the value", e);
        }
    }

    private void validateCanManageInvites(UUID workspaceId, UUID userId) {
        Workspaces workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> {
                    log.error("Workspace with ID {} not found", workspaceId);
                    return new IllegalArgumentException("Workspace not found");
                });

        if (workspace.getOwnerId().getId().equals(userId)) {
            return;
        }

        WorkspacesMembers membership = workspacesMembersRepository
                .findByWorkspaces_IdAndUser_Id(workspaceId, userId);

        if (membership == null) {
            log.error("User with ID {} is not a member of workspace {}", userId, workspaceId);
            throw new IllegalArgumentException("User is not a member of the workspace");
        }

        if (membership.getRole() != WorkspaceRole.OWNER && membership.getRole() != WorkspaceRole.ADMIN) {
            throw new IllegalArgumentException("User is not allowed to manage invites");
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
            log.warn("Invite with code {} is not active", rawCode);
            throw new IllegalArgumentException("Invite is not active");
        }

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Invite with code {} has expired", rawCode);
            throw new IllegalArgumentException("Invite has expired");
        }

        if (invite.getUsedCount() >= invite.getMaxUses()) {
            log.warn("Invite with code {} has reached its maximum uses", rawCode);
            throw new IllegalArgumentException("Invite has reached its maximum uses");
        }

        return invite;
    }

    private void validateMemberLimit(Workspaces workspace) {
        Integer limit = workspace.getLimitMembers();
        if (limit == null) {
            return;
        }

        Long currentMembers = workspacesMembersRepository.countByWorkspaces_Id(workspace.getId());
        if (currentMembers >= limit) {
            log.warn("Workspace {} has reached its member limit", workspace.getId());
            throw new IllegalArgumentException("Workspace has reached its member limit");
        }
    }
}

package com.unitrack.backend.workspaces.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.user.repository.UserRepository;
import com.unitrack.backend.workspaces.dto.AcceptInviteRequest;
import com.unitrack.backend.workspaces.dto.CreatedInviteRequest;
import com.unitrack.backend.workspaces.dto.CreatedInviteResponse;
import com.unitrack.backend.workspaces.entity.WorkspaceInvite;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.repository.WorkspaceInviteRepository;
import com.unitrack.backend.workspaces.repository.WorkspaceRepository;
import com.unitrack.backend.workspaces.repository.WorkspacesMembersRepository;

@ExtendWith(MockitoExtension.class)
class WorkspaceInviteServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceInviteRepository workspaceInviteRepository;

    @Mock
    private WorkspacesMembersRepository workspacesMembersRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private WorkspaceInviteService workspaceInviteService;

    @Test
    void createInvite_DeactivatesExistingActiveInvites_AndCreatesNewInvite() {
        UUID workspaceId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        User owner = new User();
        owner.setId(ownerId);

        Workspaces workspace = new Workspaces();
        workspace.setId(workspaceId);
        workspace.setOwnerId(owner);

        WorkspaceInvite activeInvite = new WorkspaceInvite();
        activeInvite.setId(UUID.randomUUID());
        activeInvite.setIsActive(true);
        activeInvite.setWorkspaces(workspace);

        CreatedInviteRequest request = new CreatedInviteRequest();
        request.setWorkspaceId(workspaceId);

        when(currentUserService.getAuthenticatedUser()).thenReturn(owner);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(workspaceInviteRepository.findByWorkspaces_IdAndIsActiveTrue(workspaceId))
                .thenReturn(List.of(activeInvite));
        when(workspaceInviteRepository.saveAll(anyList())).thenReturn(List.of(activeInvite));
        when(workspaceInviteRepository.save(any(WorkspaceInvite.class))).thenAnswer(invocation -> {
            WorkspaceInvite saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        CreatedInviteResponse response = workspaceInviteService.createInvite(request);

        assertFalse(activeInvite.getIsActive());
        assertNotNull(response.inviteId());
        assertNotNull(response.code());
        verify(workspaceInviteRepository).saveAll(anyList());
        verify(workspaceInviteRepository).save(any(WorkspaceInvite.class));
    }

    @Test
    void acceptInvite_SavesMembership_AndDeactivatesInvite() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User authenticatedUser = new User();
        authenticatedUser.setId(userId);

        Workspaces workspace = new Workspaces();
        workspace.setId(workspaceId);
        workspace.setLimitMembers(10);

        WorkspaceInvite invite = new WorkspaceInvite();
        invite.setId(UUID.randomUUID());
        invite.setWorkspaces(workspace);
        invite.setIsActive(true);
        invite.setExpiresAt(LocalDateTime.now().plusDays(1));
        invite.setMaxUses(1);
        invite.setUsedCount(0);

        when(currentUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(workspaceInviteRepository.findByCodeHash(anyString())).thenReturn(Optional.of(invite));
        when(workspacesMembersRepository.existsByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(false);
        when(workspacesMembersRepository.countByWorkspaces_Id(workspaceId)).thenReturn(0L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(authenticatedUser));
        when(workspacesMembersRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(workspaceInviteRepository.save(any(WorkspaceInvite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        workspaceInviteService.acceptInvite(new AcceptInviteRequest("ABC-123"));

        ArgumentCaptor<WorkspaceInvite> inviteCaptor = ArgumentCaptor.forClass(WorkspaceInvite.class);
        verify(workspaceInviteRepository).save(inviteCaptor.capture());
        WorkspaceInvite savedInvite = inviteCaptor.getValue();

        assertEquals(1, savedInvite.getUsedCount());
        assertFalse(savedInvite.getIsActive());
        verify(workspacesMembersRepository).save(any());
        verify(workspaceInviteRepository, never()).findById(any());
    }

    @Test
    void acceptInvite_ShouldFail_WhenInviteCodeIsInvalid() {
        User authenticatedUser = new User();
        authenticatedUser.setId(UUID.randomUUID());

        when(currentUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(workspaceInviteRepository.findByCodeHash(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            workspaceInviteService.acceptInvite(new AcceptInviteRequest("INVALID-CODE"));
        });

        verify(workspacesMembersRepository, never()).save(any());
        verify(workspaceInviteRepository, never()).save(any(WorkspaceInvite.class));
    }

    @Test
    void acceptInvite_ShouldFail_WhenInviteIsExpired() {
        UUID workspaceId = UUID.randomUUID();
        User authenticatedUser = new User();
        authenticatedUser.setId(UUID.randomUUID());

        Workspaces workspace = new Workspaces();
        workspace.setId(workspaceId);
        workspace.setLimitMembers(10);

        WorkspaceInvite expiredInvite = new WorkspaceInvite();
        expiredInvite.setId(UUID.randomUUID());
        expiredInvite.setWorkspaces(workspace);
        expiredInvite.setIsActive(true);
        expiredInvite.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        expiredInvite.setMaxUses(1);
        expiredInvite.setUsedCount(0);

        when(currentUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(workspaceInviteRepository.findByCodeHash(anyString())).thenReturn(Optional.of(expiredInvite));

        assertThrows(IllegalArgumentException.class, () -> {
            workspaceInviteService.acceptInvite(new AcceptInviteRequest("EXPIRED-123"));
        });

        verify(workspacesMembersRepository, never()).save(any());
        verify(workspaceInviteRepository, never()).save(any(WorkspaceInvite.class));
    }

    @Test
    void acceptInvite_ShouldFail_WhenMemberLimitIsReached() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User authenticatedUser = new User();
        authenticatedUser.setId(userId);

        Workspaces workspace = new Workspaces();
        workspace.setId(workspaceId);
        workspace.setLimitMembers(1);

        WorkspaceInvite invite = new WorkspaceInvite();
        invite.setId(UUID.randomUUID());
        invite.setWorkspaces(workspace);
        invite.setIsActive(true);
        invite.setExpiresAt(LocalDateTime.now().plusDays(1));
        invite.setMaxUses(1);
        invite.setUsedCount(0);

        when(currentUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(workspaceInviteRepository.findByCodeHash(anyString())).thenReturn(Optional.of(invite));
        when(workspacesMembersRepository.existsByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(false);
        when(workspacesMembersRepository.countByWorkspaces_Id(workspaceId)).thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () -> {
            workspaceInviteService.acceptInvite(new AcceptInviteRequest("LIMIT-123"));
        });

        verify(userRepository, never()).findById(any());
        verify(workspacesMembersRepository, never()).save(any());
        verify(workspaceInviteRepository, never()).save(any(WorkspaceInvite.class));
    }
}

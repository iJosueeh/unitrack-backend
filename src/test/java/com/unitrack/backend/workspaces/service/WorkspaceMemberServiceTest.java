package com.unitrack.backend.workspaces.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.unitrack.backend.auth.services.CurrentUserService;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.activity.event.ActivityEvent;
import com.unitrack.backend.workspaces.entity.WorkspacesMembers;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.repository.WorkspacesMembersRepository;

@ExtendWith(MockitoExtension.class)
public class WorkspaceMemberServiceTest {

    @Mock
    private WorkspacesMembersRepository workspacesMembersRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private WorkspaceMemberService workspaceMemberService;

    @Test
    void updateMemberRole_ShouldFail_WhenRequesterIsNotOwner() {
        UUID workspaceId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        WorkspacesMembers requesterMembership = new WorkspacesMembers();
        requesterMembership.setRole(WorkspaceRole.ADMIN);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(workspacesMembersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, requesterId))
                .thenReturn(requesterMembership);

        assertThrows(AccessDeniedException.class, () -> {
            workspaceMemberService.updateMemberRole(workspaceId, targetUserId, WorkspaceRole.USER);
        });
    }

    @Test
    void removeMember_ShouldDelete_WhenRequesterCanManageAndTargetIsNotOwner() {
        UUID workspaceId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        WorkspacesMembers requesterMembership = new WorkspacesMembers();
        requesterMembership.setRole(WorkspaceRole.ADMIN);

        WorkspacesMembers targetMembership = new WorkspacesMembers();
        targetMembership.setRole(WorkspaceRole.USER);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(workspacesMembersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, requesterId))
                .thenReturn(requesterMembership);
        when(workspacesMembersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, targetUserId))
                .thenReturn(targetMembership);

        workspaceMemberService.removeMember(workspaceId, targetUserId);

        verify(workspacesMembersRepository).delete(targetMembership);
        verify(publisher).publishEvent(org.mockito.ArgumentMatchers.any(ActivityEvent.class));
    }

    @Test
    void removeMember_ShouldFail_WhenTargetIsOwner() {
        UUID workspaceId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        User requester = new User();
        requester.setId(requesterId);

        WorkspacesMembers requesterMembership = new WorkspacesMembers();
        requesterMembership.setRole(WorkspaceRole.ADMIN);

        WorkspacesMembers targetMembership = new WorkspacesMembers();
        targetMembership.setRole(WorkspaceRole.OWNER);

        when(currentUserService.getAuthenticatedUser()).thenReturn(requester);
        when(workspacesMembersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, requesterId))
                .thenReturn(requesterMembership);
        when(workspacesMembersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, targetUserId))
                .thenReturn(targetMembership);

        assertThrows(IllegalArgumentException.class, () -> {
            workspaceMemberService.removeMember(workspaceId, targetUserId);
        });

        verify(workspacesMembersRepository, never()).delete(targetMembership);
    }

}
package com.unitrack.backend.workspaces.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.entity.WorkspacesMembers;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.repository.WorkspacesMembersRepository;

@ExtendWith(MockitoExtension.class)
class WorkspaceAccessPolicyTest {

    @Mock
    private WorkspacesMembersRepository membersRepository;

    @InjectMocks
    private WorkspaceAccessPolicy policy;

    @Test
    void requireMembership_ShouldReturnMembership_WhenUserIsMember() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WorkspacesMembers membership = membership(workspaceId, WorkspaceRole.USER);
        when(membersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(membership);

        assertNotNull(policy.requireMembership(workspaceId, userId));
    }

    @Test
    void requireMembership_ShouldThrow_WhenUserIsNotMember() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> policy.requireMembership(workspaceId, userId));
    }

    @Test
    void requireManagePermission_ShouldReturnMembership_WhenUserIsAdmin() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WorkspacesMembers membership = membership(workspaceId, WorkspaceRole.ADMIN);
        when(membersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(membership);

        assertNotNull(policy.requireManagePermission(workspaceId, userId));
    }

    @Test
    void requireManagePermission_ShouldReturnMembership_WhenUserIsOwner() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WorkspacesMembers membership = membership(workspaceId, WorkspaceRole.OWNER);
        when(membersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(membership);

        assertNotNull(policy.requireManagePermission(workspaceId, userId));
    }

    @Test
    void requireManagePermission_ShouldThrow_WhenUserIsRegularMember() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WorkspacesMembers membership = membership(workspaceId, WorkspaceRole.USER);
        when(membersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(membership);

        assertThrows(AccessDeniedException.class, () -> policy.requireManagePermission(workspaceId, userId));
    }

    @Test
    void requireOwner_ShouldReturnMembership_WhenUserIsOwner() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WorkspacesMembers membership = membership(workspaceId, WorkspaceRole.OWNER);
        when(membersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(membership);

        assertNotNull(policy.requireOwner(workspaceId, userId));
    }

    @Test
    void requireOwner_ShouldThrow_WhenUserIsAdmin() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WorkspacesMembers membership = membership(workspaceId, WorkspaceRole.ADMIN);
        when(membersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(membership);

        assertThrows(AccessDeniedException.class, () -> policy.requireOwner(workspaceId, userId));
    }

    @Test
    void requireOwner_ShouldThrow_WhenUserIsNotMember() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(membersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> policy.requireOwner(workspaceId, userId));
    }

    private WorkspacesMembers membership(UUID workspaceId, WorkspaceRole role) {
        Workspaces workspace = new Workspaces();
        workspace.setId(workspaceId);

        WorkspacesMembers membership = new WorkspacesMembers();
        membership.setWorkspaces(workspace);
        membership.setRole(role);
        return membership;
    }
}


package com.unitrack.backend.workspaces.security;

import com.unitrack.backend.workspaces.entity.WorkspacesMembers;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;
import com.unitrack.backend.workspaces.repository.WorkspacesMembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkspaceAccessPolicy {

    private final WorkspacesMembersRepository membersRepository;

    /**
     * Verifica que el usuario sea miembro del workspace (cualquier rol).
     * Lanza AccessDeniedException si no lo es.
     */
    public WorkspacesMembers requireMembership(UUID workspaceId, UUID userId) {
        WorkspacesMembers membership = membersRepository.findByWorkspaces_IdAndUser_Id(workspaceId, userId);
        if (membership == null) {
            throw new AccessDeniedException("User is not a member of this workspace");
        }
        return membership;
    }

    /**
     * Verifica que el usuario tenga rol OWNER o ADMIN.
     * Lanza AccessDeniedException si no tiene permisos de gestión.
     */
    public WorkspacesMembers requireManagePermission(UUID workspaceId, UUID userId) {
        WorkspacesMembers membership = requireMembership(workspaceId, userId);
        WorkspaceRole role = membership.getRole();
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new AccessDeniedException("Only OWNER or ADMIN can manage this resource");
        }
        return membership;
    }

    /**
     * Verifica que el usuario sea el OWNER del workspace.
     * Lanza AccessDeniedException si no es OWNER.
     */
    public WorkspacesMembers requireOwner(UUID workspaceId, UUID userId) {
        WorkspacesMembers membership = requireMembership(workspaceId, userId);
        if (membership.getRole() != WorkspaceRole.OWNER) {
            throw new AccessDeniedException("Only the workspace OWNER can perform this action");
        }
        return membership;
    }
}


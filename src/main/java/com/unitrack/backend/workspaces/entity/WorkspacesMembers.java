package com.unitrack.backend.workspaces.entity;

import java.sql.Timestamp;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.workspaces.enums.WorkspaceRole;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "workspaces_members",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_workspaces_members_workspace_user", columnNames = { "workspace_id", "user_id" })
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkspacesMembers extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspaces workspaces;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private WorkspaceRole role;

    private Timestamp joinedAt;

}

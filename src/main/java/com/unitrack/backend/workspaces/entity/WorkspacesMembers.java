package com.unitrack.backend.workspaces.entity;

import java.sql.Timestamp;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.workspaces.enums.Rol;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workspaces_members")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkspacesMembers extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspaces workspaces;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Rol role;

    private Timestamp joinedAt;

}

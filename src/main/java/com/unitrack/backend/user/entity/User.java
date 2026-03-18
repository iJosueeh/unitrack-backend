package com.unitrack.backend.user.entity;

import java.util.List;

import com.unitrack.backend.activity.entity.Activity;
import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.user.enums.SystemRole;
import com.unitrack.backend.user.enums.OAuth2Provider;
import com.unitrack.backend.workspaces.entity.Workspaces;
import com.unitrack.backend.workspaces.entity.WorkspacesMembers;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private SystemRole role;

    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth2_provider")
    private OAuth2Provider oAuth2Provider;

    @Column(name = "oauth2_provider_id")
    private String oAuth2ProviderId;

    @OneToOne(mappedBy = "user")
    private Profile profile;

    @OneToMany(mappedBy = "ownerId")
    private List<Workspaces> workspaces;

    @OneToMany(mappedBy = "user")
    private List<WorkspacesMembers> workspacesMembers;

    @OneToMany(mappedBy = "user")
    private List<Activity> activity;

    @OneToMany(mappedBy = "createdBy")
    private List<Projects> createdProjects;

    @OneToMany(mappedBy = "assignedTo")
    private List<Projects> assignedProjects;
}

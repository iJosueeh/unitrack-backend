package com.unitrack.backend.workspaces.entity;

import java.util.List;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workspaces")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Workspaces extends BaseEntity {

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User ownerId;

    private Integer limitMembers;

    @OneToMany(mappedBy = "workspaces")
    private List<WorkspacesMembers> members;

    @OneToMany(mappedBy = "workspaces")
    private List<Projects> projects;

}

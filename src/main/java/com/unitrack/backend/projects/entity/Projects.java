package com.unitrack.backend.projects.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import com.unitrack.backend.tasks.entity.Tasks;
import com.unitrack.backend.user.entity.User;
import com.unitrack.backend.workspaces.entity.Workspaces;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "projects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Projects extends BaseEntity {

    private String name;
    private String description;
    private String client;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private BigDecimal budget;
    private Timestamp startDate;
    private Timestamp endDate;
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    private Workspaces workspaces;

    @OneToMany(mappedBy = "project")
    private List<Tasks> tasks;

}

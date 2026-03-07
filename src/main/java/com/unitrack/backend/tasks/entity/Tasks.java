package com.unitrack.backend.tasks.entity;

import java.sql.Timestamp;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;
import com.unitrack.backend.projects.entity.Projects;
import com.unitrack.backend.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "tasks")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tasks extends BaseEntity {

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private Timestamp dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Projects project;

    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;
}

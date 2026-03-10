package com.unitrack.backend.dashboard.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.projects.entity.Projects;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_stats")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectStats extends BaseEntity {

    private Integer tasksCompletedThisWeek;
    private Integer hoursRegisteredThisWeek;
    private Integer totalTasksCompleted;
    private BigDecimal totalHoursRegistered;
    private BigDecimal overallProgressPercentage;
    private Timestamp lastUpdatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id", nullable = false, unique = true)
    private Projects project;
}

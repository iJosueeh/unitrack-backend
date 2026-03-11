package com.unitrack.backend.dashboard.entity;

import java.sql.Timestamp;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.workspaces.entity.Workspaces;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_workspace_metrics")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyWorkspaceMetrics extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspaces workspace;

    private Timestamp date;

    private Integer tasksCompletedThatDay;

    private Integer tasksTotalThatDay;

}

package com.unitrack.backend.dashboard.entity;

import java.math.BigDecimal;

import com.unitrack.backend.common.entity.BaseEntity;
import com.unitrack.backend.workspaces.entity.Workspaces;

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
@Table(name = "dashboard_stats")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", referencedColumnName = "id", nullable = false, unique = true)
    private Workspaces workspace;

    private Integer projectsCountActive;
    private Integer projectsActiveDelta;
    private Integer taskPendingCount;
    private BigDecimal overallProgressPercentage;
    private Integer membersCount;
    private Integer membersOnsiteToday;

}

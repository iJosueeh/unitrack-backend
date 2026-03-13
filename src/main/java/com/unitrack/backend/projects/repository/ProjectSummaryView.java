package com.unitrack.backend.projects.repository;

import com.unitrack.backend.common.enums.Priority;
import com.unitrack.backend.common.enums.Status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ProjectSummaryView {
    UUID getId();

    String getName();

    String getClient();

    Status getStatus();

    Priority getPriority();

    Timestamp getStartDate();

    Timestamp getEndDate();

    UUID getAssignedToId();

    String getAssignedToFirstName();

    String getAssignedToLastName();

    UUID getCreatedById();

    String getCreatedByFirstName();

    String getCreatedByLastName();

    long getTasksCount();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}

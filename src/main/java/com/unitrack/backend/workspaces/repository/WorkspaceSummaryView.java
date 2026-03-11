package com.unitrack.backend.workspaces.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface WorkspaceSummaryView {

    UUID getId();

    String getName();

    UUID getOwnerId();

    long getMembersCount();

    long getProjectsCount();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}

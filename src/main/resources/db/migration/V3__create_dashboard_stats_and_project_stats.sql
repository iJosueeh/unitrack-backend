CREATE TABLE dashboard_stats (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    workspace_id UUID NOT NULL UNIQUE,
    projects_count_active INTEGER,
    projects_active_delta INTEGER,
    task_pending_count INTEGER,
    overall_progress_percentage NUMERIC(38, 2),
    members_count INTEGER,
    members_onsite_today INTEGER,
    CONSTRAINT fk_dashboard_stats_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
);

CREATE TABLE project_stats (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    tasks_completed_this_week INTEGER,
    hours_registered_this_week INTEGER,
    total_tasks_completed INTEGER,
    total_hours_registered NUMERIC(38, 2),
    overall_progress_percentage NUMERIC(38, 2),
    last_updated_at TIMESTAMP,
    project_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_project_stats_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
);

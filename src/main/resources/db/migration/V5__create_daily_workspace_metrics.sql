-- Create daily_workspace_metrics table (many daily metrics per workspace)
CREATE TABLE daily_workspace_metrics (
    id                        UUID PRIMARY KEY,
    created_at                TIMESTAMP,
    updated_at                TIMESTAMP,
    workspace_id              UUID NOT NULL,
    date                      TIMESTAMP NOT NULL,
    tasks_completed_that_day  INTEGER,
    tasks_total_that_day      INTEGER,
    CONSTRAINT fk_daily_workspace_metrics_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
);

CREATE INDEX IF NOT EXISTS idx_daily_workspace_metrics_workspace_id 
    ON daily_workspace_metrics (workspace_id);

CREATE INDEX IF NOT EXISTS idx_daily_workspace_metrics_date 
    ON daily_workspace_metrics (date);

CREATE INDEX IF NOT EXISTS idx_daily_workspace_metrics_workspace_date 
    ON daily_workspace_metrics (workspace_id, date);

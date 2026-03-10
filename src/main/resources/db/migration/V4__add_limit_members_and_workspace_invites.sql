-- Add limit_members column to workspaces (field added to Workspaces entity)
ALTER TABLE workspaces
    ADD COLUMN IF NOT EXISTS limit_members INTEGER;

-- Create workspace_invites table
CREATE TABLE workspace_invites (
    id            UUID PRIMARY KEY,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    workspace_id  UUID         NOT NULL,
    created_by_id UUID         NOT NULL,
    code_hash     VARCHAR(255) NOT NULL UNIQUE,
    expires_at    TIMESTAMP    NOT NULL,
    is_active     BOOLEAN      NOT NULL,
    max_uses      INTEGER      NOT NULL,
    used_count    INTEGER      NOT NULL,
    CONSTRAINT fk_workspace_invites_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT fk_workspace_invites_created_by
        FOREIGN KEY (created_by_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_workspace_invites_workspace_id ON workspace_invites (workspace_id);
CREATE INDEX IF NOT EXISTS idx_workspace_invites_code_hash    ON workspace_invites (code_hash);
CREATE INDEX IF NOT EXISTS idx_workspace_invites_is_active    ON workspace_invites (is_active);
CREATE INDEX IF NOT EXISTS idx_workspace_invites_expires_at   ON workspace_invites (expires_at);

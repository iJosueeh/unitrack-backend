-- Harden workspace membership and invite constraints.

-- 1) Workspace members: required references.
ALTER TABLE workspaces_members
    ALTER COLUMN workspace_id SET NOT NULL,
    ALTER COLUMN user_id SET NOT NULL;

-- 2) Remove duplicate memberships before enforcing uniqueness.
DELETE FROM workspaces_members wm
USING workspaces_members dup
WHERE wm.id > dup.id
  AND wm.workspace_id = dup.workspace_id
  AND wm.user_id = dup.user_id;

-- 3) Enforce single membership row per user/workspace.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_workspaces_members_workspace_user'
    ) THEN
        ALTER TABLE workspaces_members
            ADD CONSTRAINT uk_workspaces_members_workspace_user
            UNIQUE (workspace_id, user_id);
    END IF;
END $$;

-- 4) Invite integrity checks.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_workspace_invites_max_uses_positive'
    ) THEN
        ALTER TABLE workspace_invites
            ADD CONSTRAINT chk_workspace_invites_max_uses_positive
            CHECK (max_uses > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_workspace_invites_used_count_range'
    ) THEN
        ALTER TABLE workspace_invites
            ADD CONSTRAINT chk_workspace_invites_used_count_range
            CHECK (used_count >= 0 AND used_count <= max_uses);
    END IF;
END $$;

-- 5) Allow only one active invite per workspace at a time.
CREATE UNIQUE INDEX IF NOT EXISTS uk_workspace_invites_active_workspace
    ON workspace_invites (workspace_id)
    WHERE is_active = true;

-- 6) Additional hardening in daily metrics (one row per workspace/day).
DELETE FROM daily_workspace_metrics d1
USING daily_workspace_metrics d2
WHERE d1.id > d2.id
  AND d1.workspace_id = d2.workspace_id
  AND d1.date = d2.date;

CREATE UNIQUE INDEX IF NOT EXISTS uk_daily_workspace_metrics_workspace_date
    ON daily_workspace_metrics (workspace_id, date);

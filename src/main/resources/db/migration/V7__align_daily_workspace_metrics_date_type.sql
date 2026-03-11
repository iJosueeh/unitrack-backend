-- Align daily_workspace_metrics.date to DATE semantics (one row per workspace per day).
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'daily_workspace_metrics'
          AND column_name = 'date'
          AND data_type <> 'date'
    ) THEN
        -- Deduplicate by calendar day before type conversion.
        DELETE FROM daily_workspace_metrics d1
        USING daily_workspace_metrics d2
        WHERE d1.id > d2.id
          AND d1.workspace_id = d2.workspace_id
          AND d1.date::date = d2.date::date;

        ALTER TABLE daily_workspace_metrics
            ALTER COLUMN date TYPE DATE USING date::date;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_daily_workspace_metrics_workspace_date
    ON daily_workspace_metrics (workspace_id, date);

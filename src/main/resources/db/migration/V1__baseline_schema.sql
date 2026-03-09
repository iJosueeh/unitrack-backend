-- Baseline schema for current JPA models.

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50),
    is_active BOOLEAN
);

CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id UUID NOT NULL UNIQUE,
    bio VARCHAR(255),
    image_url VARCHAR(255),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS workspaces (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    name VARCHAR(255),
    owner_id UUID,
    CONSTRAINT fk_workspaces_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS workspaces_members (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    workspace_id UUID,
    user_id UUID,
    role VARCHAR(50),
    joined_at TIMESTAMP,
    CONSTRAINT fk_workspace_members_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    CONSTRAINT fk_workspace_members_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    name VARCHAR(255),
    description VARCHAR(255),
    client VARCHAR(255),
    status VARCHAR(50),
    priority VARCHAR(50),
    budget NUMERIC(38, 2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    created_by UUID,
    assigned_to UUID,
    workspace_id UUID,
    CONSTRAINT fk_projects_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_projects_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id),
    CONSTRAINT fk_projects_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
);

CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    title VARCHAR(255),
    description VARCHAR(255),
    status VARCHAR(50),
    priority VARCHAR(50),
    due_date TIMESTAMP,
    project_id UUID,
    assigned_to_id UUID,
    created_by_id UUID,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_tasks_assigned_to FOREIGN KEY (assigned_to_id) REFERENCES users(id),
    CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id UUID,
    task_id UUID,
    message VARCHAR(255),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks(id)
);

CREATE TABLE IF NOT EXISTS attachments (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    file_url VARCHAR(255),
    task_id UUID NOT NULL,
    uploader_by_id UUID,
    CONSTRAINT fk_attachments_task FOREIGN KEY (task_id) REFERENCES tasks(id),
    CONSTRAINT fk_attachments_uploader FOREIGN KEY (uploader_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS activity (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    user_id UUID,
    action VARCHAR(50),
    entity_type VARCHAR(50),
    entity_id UUID,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_workspaces_owner_id ON workspaces(owner_id);
CREATE INDEX IF NOT EXISTS idx_workspace_members_workspace_id ON workspaces_members(workspace_id);
CREATE INDEX IF NOT EXISTS idx_workspace_members_user_id ON workspaces_members(user_id);
CREATE INDEX IF NOT EXISTS idx_projects_workspace_id ON projects(workspace_id);
CREATE INDEX IF NOT EXISTS idx_projects_created_by ON projects(created_by);
CREATE INDEX IF NOT EXISTS idx_projects_assigned_to ON projects(assigned_to);
CREATE INDEX IF NOT EXISTS idx_tasks_project_id ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to_id ON tasks(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_tasks_created_by_id ON tasks(created_by_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_task_id ON comments(task_id);
CREATE INDEX IF NOT EXISTS idx_attachments_task_id ON attachments(task_id);
CREATE INDEX IF NOT EXISTS idx_attachments_uploader_by_id ON attachments(uploader_by_id);
CREATE INDEX IF NOT EXISTS idx_activity_user_id ON activity(user_id);

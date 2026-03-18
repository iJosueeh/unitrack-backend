-- V8__add_oauth2_fields_to_users.sql
-- Agregar soporte para OAuth2 en la tabla users

ALTER TABLE users
ADD COLUMN oauth2_provider VARCHAR(50);

ALTER TABLE users
ADD COLUMN oauth2_provider_id VARCHAR(255);

-- Crear índice único para (email, oauth2_provider, oauth2_provider_id)
CREATE UNIQUE INDEX idx_users_oauth2_provider_id
ON users (oauth2_provider, oauth2_provider_id)
WHERE oauth2_provider IS NOT NULL;


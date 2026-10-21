-- TrustVault Initial Schema
-- V1__initial_schema.sql

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL, -- ADMIN, OWNER, VIEWER
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, DISABLED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- 2. Documents Table
CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id),
    original_display_name VARCHAR(255) NOT NULL,
    safe_storage_object_key VARCHAR(255) NOT NULL UNIQUE,
    mime_type VARCHAR(128) NOT NULL,
    file_size BIGINT NOT NULL,
    category VARCHAR(64) NOT NULL,
    description TEXT,
    sha256_checksum VARCHAR(64) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_documents_owner_id ON documents(owner_id);
CREATE INDEX IF NOT EXISTS idx_documents_category ON documents(category);
CREATE INDEX IF NOT EXISTS idx_documents_is_deleted ON documents(is_deleted);
CREATE INDEX IF NOT EXISTS idx_documents_created_at ON documents(created_at);

-- 3. Document Grants Table
CREATE TABLE IF NOT EXISTS document_grants (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    recipient_user_id UUID NOT NULL REFERENCES users(id),
    permission VARCHAR(32) NOT NULL, -- READ, EDIT
    granting_user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    revocation_timestamp TIMESTAMP WITH TIME ZONE,
    revoking_user_id UUID REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_grants_doc_recipient ON document_grants(document_id, recipient_user_id);
CREATE INDEX IF NOT EXISTS idx_grants_revocation ON document_grants(revocation_timestamp);

-- Composite index for fast active grant lookups
CREATE INDEX IF NOT EXISTS idx_unique_active_grant 
ON document_grants(document_id, recipient_user_id, revocation_timestamp);

-- 4. Audit Events Table (Tamper-evident Hash Chain)
CREATE TABLE IF NOT EXISTS audit_events (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actor_id UUID,
    action VARCHAR(64) NOT NULL,
    document_id UUID,
    target_user_id UUID,
    outcome VARCHAR(32) NOT NULL, -- SUCCESS, DENIAL
    correlation_id VARCHAR(64) NOT NULL,
    details TEXT,
    previous_event_hash VARCHAR(64) NOT NULL,
    current_event_hash VARCHAR(64) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_actor_id ON audit_events(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_document_id ON audit_events(document_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_events(action);

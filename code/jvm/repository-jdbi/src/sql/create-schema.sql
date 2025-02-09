-- Create the schema dbo
CREATE SCHEMA IF NOT EXISTS dbo;

-- Create table for users in the dbo schema
CREATE TABLE dbo.users
(
    id                  SERIAL PRIMARY KEY,
    username            VARCHAR(30) UNIQUE NOT NULL CHECK (LENGTH(username) BETWEEN 1 AND 30),
    password_validation VARCHAR(255)       NOT NULL
);

-- Create table for events in the dbo schema
CREATE TABLE dbo.channels
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(30) UNIQUE NOT NULL CHECK (LENGTH(name) BETWEEN 1 AND 30),
    owner_id  INT                NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    is_public BOOLEAN DEFAULT TRUE
);

-- Create table for messages in the dbo schema
CREATE TABLE dbo.messages
(
    id         SERIAL PRIMARY KEY,
    content    TEXT NOT NULL CHECK (LENGTH(content) BETWEEN 1 AND 1000),
    user_id    INT  REFERENCES dbo.users (id) ON DELETE SET NULL,
    channel_id INT  NOT NULL REFERENCES dbo.channels (id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create table for invitations in the dbo schema
CREATE TABLE dbo.invitations
(
    id          SERIAL PRIMARY KEY,
    token       VARCHAR(100) UNIQUE NOT NULL,
    created_by  INT                 NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    channel_id  INT                 NOT NULL REFERENCES dbo.channels (id) ON DELETE CASCADE,
    access_type VARCHAR(10)         NOT NULL CHECK (access_type IN ('read-only', 'read-write')),
    expires_at  TIMESTAMP           NOT NULL CHECK (expires_at > CURRENT_TIMESTAMP),
    status      VARCHAR(10)         NOT NULL CHECK (status IN ('pending', 'accepted', 'rejected')) DEFAULT 'pending'
);


-- Create table for membership in the dbo schema
CREATE TABLE dbo.channel_members
(
    id          SERIAL PRIMARY KEY,
    user_id     INT         NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    channel_id  INT         NOT NULL REFERENCES dbo.channels (id) ON DELETE CASCADE,
    access_type VARCHAR(10) NOT NULL CHECK (access_type IN ('read-only', 'read-write')),
    UNIQUE (user_id, channel_id)
);

-- Create table for tokens in the dbo schema
CREATE TABLE dbo.tokens
(
    token_validation VARCHAR(256) PRIMARY KEY,
    user_id          INT    NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    created_at       BIGINT NOT NULL,
    last_used_at     BIGINT NOT NULL
);

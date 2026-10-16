DROP TABLE app_info;

CREATE TABLE users (
    id  BIGSERIAL PRIMARY KEY,
    email   VARCHAR(255) NOT NULL UNIQUE,
    display_name    VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE campaigns (
    id  BIGSERIAL PRIMARY KEY,
    name    VARCHAR(150) NOT NULL,
    description TEXT,
    invite_code VARCHAR(20) NOT NULL UNIQUE,
    created_by BIGINT NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE campaign_members (
    id BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(20) NOT NULL CHECK (role IN ('GUARDIAN', 'PLAYER')),
    joined_at   TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_campaign_member UNIQUE (campaign_id, user_id)
);

CREATE INDEX idx_campaign_members_user ON campaign_members (user_id);
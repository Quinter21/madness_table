CREATE TABLE campaign_skills (
    id          BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES campaigns (id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    base_value  INTEGER NOT NULL CHECK (base_value BETWEEN 0 AND 100),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_campaign_skill_name ON campaign_skills (campaign_id, lower(name));

CREATE TABLE investigators (
    id          BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES campaigns (id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    occupation  VARCHAR(100) NOT NULL,
    age         INTEGER NOT NULL CHECK (age BETWEEN 1 and 120),
    backstory   TEXT,

    -- CARACTERÍSTICAS --
    strength        INTEGER NOT NULL CHECK (strength BETWEEN 1 AND 99),
    constitution    INTEGER NOT NULL CHECK (constitution BETWEEN 1 AND 99),
    size            INTEGER NOT NULL CHECK (size BETWEEN 1 AND 99),
    dexterity       INTEGER NOT NULL CHECK (dexterity BETWEEN 1 AND 99),
    appearance      INTEGER NOT NULL CHECK (appearance BETWEEN 1 AND 99),
    intelligence    INTEGER NOT NULL CHECK (intelligence BETWEEN 1 AND 99),
    power           INTEGER NOT NULL CHECK (power BETWEEN 1 AND 99),
    education       INTEGER NOT NULL CHECK (education BETWEEN 1 AND 99),

    -- OTROS VALORES IMPORTANTES --
    hit_points      INTEGER NOT NULL CHECK (hit_points BETWEEN 0 AND 20),
    magic_points    INTEGER NOT NULL CHECK (magic_points BETWEEN 0 AND 25),
    sanity          INTEGER NOT NULL CHECK (sanity BETWEEN 0 AND 99),
    luck            INTEGER NOT NULL CHECK (luck BETWEEN 0 AND 99),

    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_investigators_campaign ON investigators (campaign_id);
CREATE INDEX idx_investigators_user ON investigators (user_id);

CREATE TABLE investigator_skills (
    id                  BIGSERIAL PRIMARY KEY,
    investigator_id     BIGINT NOT NULL REFERENCES investigators (id) ON DELETE CASCADE,
    campaign_skill_id   BIGINT NOT NULL REFERENCES campaign_skills (id),
    value               INTEGER NOT NULL CHECK (value BETWEEN 0 AND 100),
    CONSTRAINT uq_investigator_skill UNIQUE (investigator_id, campaign_skill_id)
);

CREATE INDEX idx_investigator_skills_skill ON investigator_skills (campaign_skill_id);

CREATE TABLE roll_logs (
    id                  BIGSERIAL PRIMARY KEY,
    investigator_id     BIGINT NOT NULL REFERENCES investigators (id) ON DELETE CASCADE,
    rolled_by           BIGINT NOT NULL REFERENCES users (id),
    skill_name          VARCHAR(100) NOT NULL,
    skill_value         INTEGER NOT NULL CHECK (skill_value >= 0),
    roll_result         INTEGER NOT NULL CHECK (roll_result BETWEEN 1 AND 100),
    net_dice            INTEGER NOT NULL CHECK (net_dice BETWEEN -2 AND 2),
    success_level       VARCHAR(20) NOT NULL
                        CHECK (success_level IN ('FUMBLE', 'FAILURE', 'REGULAR',
                                                 'HARD', 'EXTREME', 'CRITICAL')),
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_roll_logs_investigator ON roll_logs (investigator_id, created_at);
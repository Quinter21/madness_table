CREATE TABLE join_requests (
    id          BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT NOT NULL REFERENCES campaigns (id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    resolved_at TIMESTAMP
);

CREATE UNIQUE INDEX uq_join_request_pending
    ON join_requests (campaign_id, user_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_join_requests_user ON join_requests (user_id);
CREATE INDEX idx_join_requests_campaign_status ON join_requests (campaign_id, status);
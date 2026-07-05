ALTER TABLE notification_outbox
    ADD COLUMN claimed_at TIMESTAMPTZ;

CREATE INDEX notification_outbox_processing_claimed_at_idx
    ON notification_outbox (status, claimed_at);

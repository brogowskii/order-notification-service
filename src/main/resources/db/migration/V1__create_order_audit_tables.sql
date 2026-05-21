CREATE TABLE order_request_audit (
    request_id UUID PRIMARY KEY,
    shipment_number VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(320) NOT NULL,
    recipient_country_code CHAR(2) NOT NULL,
    sender_country_code CHAR(2) NOT NULL,
    status_code SMALLINT NOT NULL CHECK (status_code BETWEEN 0 AND 100),
    received_at TIMESTAMPTZ NOT NULL,
    stored_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX order_request_audit_shipment_number_idx
    ON order_request_audit (shipment_number);

CREATE TABLE notification_outbox (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE REFERENCES order_request_audit (request_id),
    shipment_number VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(320) NOT NULL,
    recipient_country_code CHAR(2) NOT NULL,
    sender_country_code CHAR(2) NOT NULL,
    status_code SMALLINT NOT NULL CHECK (status_code BETWEEN 0 AND 100),
    requested_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    CONSTRAINT notification_outbox_status_check
        CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX notification_outbox_pending_idx
    ON notification_outbox (status, next_attempt_at);

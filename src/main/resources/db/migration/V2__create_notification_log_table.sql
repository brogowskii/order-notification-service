CREATE TABLE notification_log (
    request_id UUID PRIMARY KEY,
    shipment_number VARCHAR(100) NOT NULL,
    recipient_email VARCHAR(320) NOT NULL,
    recipient_country_code CHAR(2) NOT NULL,
    sender_country_code CHAR(2) NOT NULL,
    status_code SMALLINT NOT NULL CHECK (status_code BETWEEN 0 AND 100),
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT notification_log_status_check
        CHECK (status IN ('SENT', 'FAILED'))
);

CREATE INDEX notification_log_recipient_email_idx
    ON notification_log (recipient_email);

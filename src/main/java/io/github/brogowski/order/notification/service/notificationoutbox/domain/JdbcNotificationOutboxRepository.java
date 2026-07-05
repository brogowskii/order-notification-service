package io.github.brogowski.order.notification.service.notificationoutbox.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

class JdbcNotificationOutboxRepository implements NotificationOutboxRepository {

    private final JdbcClient jdbcClient;

    JdbcNotificationOutboxRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(NotificationOutboxEntry entry) {
        jdbcClient
                .sql("""
            INSERT INTO notification_outbox (
                id,
                request_id,
                shipment_number,
                recipient_email,
                recipient_country_code,
                sender_country_code,
                status_code,
                requested_at,
                status,
                attempts,
                created_at,
                next_attempt_at,
                claimed_at,
                published_at
            ) VALUES (
                :id,
                :requestId,
                :shipmentNumber,
                :recipientEmail,
                :recipientCountryCode,
                :senderCountryCode,
                :statusCode,
                :requestedAt,
                :status,
                :attempts,
                :createdAt,
                :nextAttemptAt,
                :claimedAt,
                :publishedAt
            )
            ON CONFLICT (request_id) DO NOTHING
            """)
                .param("id", entry.id())
                .param("requestId", entry.requestId())
                .param("shipmentNumber", entry.shipmentNumber())
                .param("recipientEmail", entry.recipientEmail())
                .param("recipientCountryCode", entry.recipientCountryCode())
                .param("senderCountryCode", entry.senderCountryCode())
                .param("statusCode", entry.statusCode())
                .param("requestedAt", timestamp(entry.requestedAt()))
                .param("status", entry.status().name())
                .param("attempts", entry.attempts())
                .param("createdAt", timestamp(entry.createdAt()))
                .param("nextAttemptAt", timestamp(entry.nextAttemptAt()))
                .param("claimedAt", timestampOrNull(entry.claimedAt()))
                .param("publishedAt", timestampOrNull(entry.publishedAt()))
                .update();
    }

    @Override
    public List<NotificationOutboxEntry> claimPending(Instant now, Instant processingExpiredBefore, int limit) {
        return jdbcClient
                .sql("""
            WITH claimed AS (
                SELECT id
                FROM notification_outbox
                WHERE (
                    status = :pendingStatus
                    AND next_attempt_at <= :now
                ) OR (
                    status = :processingStatus
                    AND (claimed_at IS NULL OR claimed_at <= :processingExpiredBefore)
                )
                ORDER BY created_at
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
            )
            UPDATE notification_outbox
            SET status = :processingStatus,
                claimed_at = :now
            FROM claimed
            WHERE notification_outbox.id = claimed.id
            RETURNING
                notification_outbox.id,
                notification_outbox.request_id,
                notification_outbox.shipment_number,
                notification_outbox.recipient_email,
                notification_outbox.recipient_country_code,
                notification_outbox.sender_country_code,
                notification_outbox.status_code,
                notification_outbox.requested_at,
                notification_outbox.status,
                notification_outbox.attempts,
                notification_outbox.created_at,
                notification_outbox.next_attempt_at,
                notification_outbox.claimed_at,
                notification_outbox.published_at
            """)
                .param("pendingStatus", OutboxStatus.PENDING.name())
                .param("processingStatus", OutboxStatus.PROCESSING.name())
                .param("now", timestamp(now))
                .param("processingExpiredBefore", timestamp(processingExpiredBefore))
                .param("limit", limit)
                .query(this::mapRow)
                .list();
    }

    @Override
    public void markPublished(UUID id, Instant publishedAt) {
        jdbcClient
                .sql("""
            UPDATE notification_outbox
            SET status = :status,
                claimed_at = NULL,
                published_at = :publishedAt
            WHERE id = :id
              AND status = :processingStatus
            """)
                .param("status", OutboxStatus.PUBLISHED.name())
                .param("processingStatus", OutboxStatus.PROCESSING.name())
                .param("id", id)
                .param("publishedAt", timestamp(publishedAt))
                .update();
    }

    @Override
    public void markFailed(UUID id, Instant nextAttemptAt, int maxAttempts) {
        jdbcClient
                .sql("""
            UPDATE notification_outbox
            SET attempts = attempts + 1,
                status = CASE
                    WHEN attempts + 1 >= :maxAttempts THEN :failedStatus
                    ELSE :pendingStatus
                END,
                claimed_at = NULL,
                next_attempt_at = :nextAttemptAt
            WHERE id = :id
              AND status = :processingStatus
            """)
                .param("id", id)
                .param("nextAttemptAt", timestamp(nextAttemptAt))
                .param("maxAttempts", maxAttempts)
                .param("failedStatus", OutboxStatus.FAILED.name())
                .param("pendingStatus", OutboxStatus.PENDING.name())
                .param("processingStatus", OutboxStatus.PROCESSING.name())
                .update();
    }

    private NotificationOutboxEntry mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new NotificationOutboxEntry(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("request_id", UUID.class),
                resultSet.getString("shipment_number"),
                resultSet.getString("recipient_email"),
                resultSet.getString("recipient_country_code"),
                resultSet.getString("sender_country_code"),
                resultSet.getInt("status_code"),
                resultSet.getTimestamp("requested_at").toInstant(),
                OutboxStatus.valueOf(resultSet.getString("status")),
                resultSet.getInt("attempts"),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("next_attempt_at").toInstant(),
                timestampToInstant(resultSet.getTimestamp("claimed_at")),
                timestampToInstant(resultSet.getTimestamp("published_at")));
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    private static Timestamp timestampOrNull(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant timestampToInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}

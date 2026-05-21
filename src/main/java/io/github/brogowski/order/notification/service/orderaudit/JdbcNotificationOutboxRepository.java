package io.github.brogowski.order.notification.service.orderaudit;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class JdbcNotificationOutboxRepository implements NotificationOutboxRepository {

  private final JdbcClient jdbcClient;

  JdbcNotificationOutboxRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public void save(NotificationOutboxEntry entry) {
    jdbcClient
        .sql(
            """
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
        .param("publishedAt", timestampOrNull(entry.publishedAt()))
        .update();
  }

  private static Timestamp timestamp(Instant instant) {
    return Timestamp.from(instant);
  }

  private static Timestamp timestampOrNull(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }
}

package io.github.brogowski.order.notification.service.notification;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class JdbcNotificationLogRepository implements NotificationLogRepository {

  private final JdbcClient jdbcClient;

  JdbcNotificationLogRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  @Override
  public void save(NotificationLog log) {
    jdbcClient
        .sql(
            """
            INSERT INTO notification_log (
                request_id,
                shipment_number,
                recipient_email,
                recipient_country_code,
                sender_country_code,
                status_code,
                subject,
                body,
                status,
                requested_at,
                sent_at
            ) VALUES (
                :requestId,
                :shipmentNumber,
                :recipientEmail,
                :recipientCountryCode,
                :senderCountryCode,
                :statusCode,
                :subject,
                :body,
                :status,
                :requestedAt,
                :sentAt
            )
            ON CONFLICT (request_id) DO NOTHING
            """)
        .param("requestId", log.requestId())
        .param("shipmentNumber", log.shipmentNumber())
        .param("recipientEmail", log.recipientEmail())
        .param("recipientCountryCode", log.recipientCountryCode())
        .param("senderCountryCode", log.senderCountryCode())
        .param("statusCode", log.statusCode())
        .param("subject", log.subject())
        .param("body", log.body())
        .param("status", log.status().name())
        .param("requestedAt", timestamp(log.requestedAt()))
        .param("sentAt", timestamp(log.sentAt()))
        .update();
  }

  @Override
  public Optional<NotificationLog> findByRequestId(UUID requestId) {
    return jdbcClient
        .sql(
            """
            SELECT
                request_id,
                shipment_number,
                recipient_email,
                recipient_country_code,
                sender_country_code,
                status_code,
                subject,
                body,
                status,
                requested_at,
                sent_at
            FROM notification_log
            WHERE request_id = :requestId
            """)
        .param("requestId", requestId)
        .query(this::mapRow)
        .optional();
  }

  private NotificationLog mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
    return new NotificationLog(
        resultSet.getObject("request_id", UUID.class),
        resultSet.getString("shipment_number"),
        resultSet.getString("recipient_email"),
        resultSet.getString("recipient_country_code"),
        resultSet.getString("sender_country_code"),
        resultSet.getInt("status_code"),
        resultSet.getString("subject"),
        resultSet.getString("body"),
        NotificationStatus.valueOf(resultSet.getString("status")),
        resultSet.getTimestamp("requested_at").toInstant(),
        resultSet.getTimestamp("sent_at").toInstant());
  }

  private static Timestamp timestamp(Instant instant) {
    return Timestamp.from(instant);
  }
}

package io.github.brogowski.order.notification.service.orderaudit.domain;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

class JdbcOrderRequestAuditRepository implements OrderRequestAuditRepository {

    private final JdbcClient jdbcClient;

    JdbcOrderRequestAuditRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(OrderRequestAudit audit) {
        jdbcClient
                .sql("""
            INSERT INTO order_request_audit (
                request_id,
                shipment_number,
                recipient_email,
                recipient_country_code,
                sender_country_code,
                status_code,
                received_at,
                stored_at
            ) VALUES (
                :requestId,
                :shipmentNumber,
                :recipientEmail,
                :recipientCountryCode,
                :senderCountryCode,
                :statusCode,
                :receivedAt,
                :storedAt
            )
            ON CONFLICT (request_id) DO NOTHING
            """)
                .param("requestId", audit.requestId())
                .param("shipmentNumber", audit.shipmentNumber())
                .param("recipientEmail", audit.recipientEmail())
                .param("recipientCountryCode", audit.recipientCountryCode())
                .param("senderCountryCode", audit.senderCountryCode())
                .param("statusCode", audit.statusCode())
                .param("receivedAt", timestamp(audit.receivedAt()))
                .param("storedAt", timestamp(audit.storedAt()))
                .update();
    }

    @Override
    public Optional<OrderRequestAudit> findByRequestId(UUID requestId) {
        return jdbcClient
                .sql("""
            SELECT
                request_id,
                shipment_number,
                recipient_email,
                recipient_country_code,
                sender_country_code,
                status_code,
                received_at,
                stored_at
            FROM order_request_audit
            WHERE request_id = :requestId
            """)
                .param("requestId", requestId)
                .query(this::mapRow)
                .optional();
    }

    private OrderRequestAudit mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        return new OrderRequestAudit(
                resultSet.getObject("request_id", UUID.class),
                resultSet.getString("shipment_number"),
                resultSet.getString("recipient_email"),
                resultSet.getString("recipient_country_code"),
                resultSet.getString("sender_country_code"),
                resultSet.getInt("status_code"),
                resultSet.getTimestamp("received_at").toInstant(),
                resultSet.getTimestamp("stored_at").toInstant());
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.from(instant);
    }
}

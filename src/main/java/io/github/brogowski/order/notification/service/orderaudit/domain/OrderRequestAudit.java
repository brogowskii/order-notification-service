package io.github.brogowski.order.notification.service.orderaudit.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderaudit.dto.OrderRequestAuditDto;
import java.time.Instant;
import java.util.UUID;

record OrderRequestAudit(
        UUID requestId,
        String shipmentNumber,
        String recipientEmail,
        String recipientCountryCode,
        String senderCountryCode,
        int statusCode,
        Instant receivedAt,
        Instant storedAt) {

    static OrderRequestAudit from(OrderReceivedMessage message, Instant storedAt) {
        return new OrderRequestAudit(
                message.requestId(),
                message.shipmentNumber(),
                message.recipientEmail(),
                message.recipientCountryCode(),
                message.senderCountryCode(),
                message.statusCode(),
                message.receivedAt(),
                storedAt);
    }

    OrderRequestAuditDto toDto() {
        return new OrderRequestAuditDto(
                requestId,
                shipmentNumber,
                recipientEmail,
                recipientCountryCode,
                senderCountryCode,
                statusCode,
                receivedAt,
                storedAt);
    }
}

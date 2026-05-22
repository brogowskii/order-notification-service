package io.github.brogowski.order.notification.service.orderaudit;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import java.time.Instant;
import java.util.UUID;

record NotificationOutboxEntry(
    UUID id,
    UUID requestId,
    String shipmentNumber,
    String recipientEmail,
    String recipientCountryCode,
    String senderCountryCode,
    int statusCode,
    Instant requestedAt,
    OutboxStatus status,
    int attempts,
    Instant createdAt,
    Instant nextAttemptAt,
    Instant publishedAt) {

  static NotificationOutboxEntry from(OrderRequestAudit audit, Instant createdAt) {
    return new NotificationOutboxEntry(
        UUID.randomUUID(),
        audit.requestId(),
        audit.shipmentNumber(),
        audit.recipientEmail(),
        audit.recipientCountryCode(),
        audit.senderCountryCode(),
        audit.statusCode(),
        createdAt,
        OutboxStatus.PENDING,
        0,
        createdAt,
        createdAt,
        null);
  }

  NotificationRequestedMessage toMessage() {
    return new NotificationRequestedMessage(
        requestId,
        shipmentNumber,
        recipientEmail,
        recipientCountryCode,
        senderCountryCode,
        statusCode,
        requestedAt);
  }
}

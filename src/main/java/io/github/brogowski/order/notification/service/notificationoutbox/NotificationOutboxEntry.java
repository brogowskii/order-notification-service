package io.github.brogowski.order.notification.service.notificationoutbox;

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

  static NotificationOutboxEntry from(NotificationOutboxCommand command) {
    return new NotificationOutboxEntry(
        UUID.randomUUID(),
        command.requestId(),
        command.shipmentNumber(),
        command.recipientEmail(),
        command.recipientCountryCode(),
        command.senderCountryCode(),
        command.statusCode(),
        command.requestedAt(),
        OutboxStatus.PENDING,
        0,
        command.requestedAt(),
        command.requestedAt(),
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

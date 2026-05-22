package io.github.brogowski.order.notification.service.notification;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import java.time.Instant;
import java.util.UUID;

record NotificationLog(
    UUID requestId,
    String shipmentNumber,
    String recipientEmail,
    String recipientCountryCode,
    String senderCountryCode,
    int statusCode,
    String subject,
    String body,
    NotificationStatus status,
    Instant requestedAt,
    Instant sentAt) {

  static NotificationLog sent(
      NotificationRequestedMessage message, EmailMessage emailMessage, Instant sentAt) {
    return new NotificationLog(
        message.requestId(),
        message.shipmentNumber(),
        message.recipientEmail(),
        message.recipientCountryCode(),
        message.senderCountryCode(),
        message.statusCode(),
        emailMessage.subject(),
        emailMessage.body(),
        NotificationStatus.SENT,
        message.requestedAt(),
        sentAt);
  }

  NotificationLogDto toDto() {
    return new NotificationLogDto(
        requestId,
        shipmentNumber,
        recipientEmail,
        recipientCountryCode,
        senderCountryCode,
        statusCode,
        subject,
        body,
        status.name(),
        requestedAt,
        sentAt);
  }
}

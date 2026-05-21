package io.github.brogowski.order.notification.service.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationLogDto(
    UUID requestId,
    String shipmentNumber,
    String recipientEmail,
    String recipientCountryCode,
    String senderCountryCode,
    int statusCode,
    String subject,
    String body,
    String status,
    Instant requestedAt,
    Instant sentAt) {}

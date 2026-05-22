package io.github.brogowski.order.notification.service.notificationoutbox;

import java.time.Instant;
import java.util.UUID;

public record NotificationOutboxCommand(
    UUID requestId,
    String shipmentNumber,
    String recipientEmail,
    String recipientCountryCode,
    String senderCountryCode,
    int statusCode,
    Instant requestedAt) {}

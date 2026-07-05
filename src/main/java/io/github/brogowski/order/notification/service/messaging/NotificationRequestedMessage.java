package io.github.brogowski.order.notification.service.messaging;

import java.time.Instant;
import java.util.UUID;

public record NotificationRequestedMessage(
        UUID requestId,
        String shipmentNumber,
        String recipientEmail,
        String recipientCountryCode,
        String senderCountryCode,
        int statusCode,
        Instant requestedAt) {}

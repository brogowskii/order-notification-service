package io.github.brogowski.order.notification.service.orderaudit;

import java.time.Instant;
import java.util.UUID;

public record OrderRequestAuditDto(
    UUID requestId,
    String shipmentNumber,
    String recipientEmail,
    String recipientCountryCode,
    String senderCountryCode,
    int statusCode,
    Instant receivedAt,
    Instant storedAt) {}

package io.github.brogowski.order.notification.service.notification.domain;

import io.github.brogowski.order.notification.service.notification.dto.NotificationLogDto;
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

    static NotificationLog sent(NotificationRequest request, EmailMessage emailMessage, Instant sentAt) {
        return new NotificationLog(
                request.requestId(),
                request.shipmentNumber(),
                request.recipientEmail(),
                request.recipientCountryCode(),
                request.senderCountryCode(),
                request.statusCode(),
                emailMessage.subject(),
                emailMessage.body(),
                NotificationStatus.SENT,
                request.requestedAt(),
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

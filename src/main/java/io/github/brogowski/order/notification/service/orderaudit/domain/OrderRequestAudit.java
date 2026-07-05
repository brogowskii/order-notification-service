package io.github.brogowski.order.notification.service.orderaudit.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderaudit.dto.OrderRequestAuditDto;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.util.StringUtils;

record OrderRequestAudit(
        UUID requestId,
        String shipmentNumber,
        String recipientEmail,
        String recipientCountryCode,
        String senderCountryCode,
        int statusCode,
        Instant receivedAt,
        Instant storedAt) {

    private static final int SHIPMENT_NUMBER_MAX_LENGTH = 100;
    private static final int RECIPIENT_EMAIL_MAX_LENGTH = 320;
    private static final String COUNTRY_CODE_PATTERN = "^[A-Z]{2}$";

    OrderRequestAudit {
        requestId = Objects.requireNonNull(requestId, "Request id must not be null");
        shipmentNumber = requireText(shipmentNumber, "Shipment number");
        if (shipmentNumber.length() > SHIPMENT_NUMBER_MAX_LENGTH) {
            throw new IllegalArgumentException("Shipment number must not be longer than 100 characters");
        }
        recipientEmail = requireText(recipientEmail, "Recipient email");
        if (!recipientEmail.contains("@")) {
            throw new IllegalArgumentException("Recipient email must be valid");
        }
        if (recipientEmail.length() > RECIPIENT_EMAIL_MAX_LENGTH) {
            throw new IllegalArgumentException("Recipient email must not be longer than 320 characters");
        }
        recipientCountryCode = requireCountryCode(recipientCountryCode);
        senderCountryCode = requireCountryCode(senderCountryCode);
        if (statusCode < 0 || statusCode > 100) {
            throw new IllegalArgumentException("Status code must be between 0 and 100");
        }
        receivedAt = Objects.requireNonNull(receivedAt, "Received at must not be null");
        storedAt = Objects.requireNonNull(storedAt, "Stored at must not be null");
    }

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

    private static String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String requireCountryCode(String value) {
        if (value == null || !value.matches(COUNTRY_CODE_PATTERN)) {
            throw new IllegalArgumentException("Country code must use ISO alpha-2 uppercase format");
        }
        return value;
    }
}

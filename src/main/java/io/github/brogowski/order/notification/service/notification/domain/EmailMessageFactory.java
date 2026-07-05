package io.github.brogowski.order.notification.service.notification.domain;

class EmailMessageFactory {

    EmailMessage create(NotificationRequest request) {
        return new EmailMessage(
                request.recipientEmail(), "Shipment " + request.shipmentNumber() + " status update", """
        Shipment number: %s
        Recipient email: %s
        Recipient country: %s
        Sender country: %s
        Status code: %d
        """.formatted(
                                request.shipmentNumber(),
                                request.recipientEmail(),
                                request.recipientCountryCode(),
                                request.senderCountryCode(),
                                request.statusCode()));
    }
}

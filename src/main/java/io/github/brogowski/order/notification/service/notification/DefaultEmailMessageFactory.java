package io.github.brogowski.order.notification.service.notification;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import org.springframework.stereotype.Component;

@Component
class DefaultEmailMessageFactory implements EmailMessageFactory {

  @Override
  public EmailMessage create(NotificationRequestedMessage message) {
    return new EmailMessage(
        message.recipientEmail(),
        "Shipment " + message.shipmentNumber() + " status update",
        """
        Shipment number: %s
        Recipient email: %s
        Recipient country: %s
        Sender country: %s
        Status code: %d
        """
            .formatted(
                message.shipmentNumber(),
                message.recipientEmail(),
                message.recipientCountryCode(),
                message.senderCountryCode(),
                message.statusCode()));
  }
}

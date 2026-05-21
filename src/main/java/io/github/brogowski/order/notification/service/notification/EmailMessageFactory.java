package io.github.brogowski.order.notification.service.notification;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;

interface EmailMessageFactory {

  EmailMessage create(NotificationRequestedMessage message);
}

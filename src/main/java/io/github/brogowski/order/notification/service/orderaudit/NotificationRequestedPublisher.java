package io.github.brogowski.order.notification.service.orderaudit;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;

interface NotificationRequestedPublisher {

  void publish(NotificationRequestedMessage message);
}

package io.github.brogowski.order.notification.service.notificationoutbox;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;

interface NotificationRequestedPublisher {

  void publish(NotificationRequestedMessage message);
}

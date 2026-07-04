package io.github.brogowski.order.notification.service.notificationoutbox.domain;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;

interface NotificationRequestedPublisher {

  void publish(NotificationRequestedMessage message);
}

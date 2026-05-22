package io.github.brogowski.order.notification.service.notification;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class NotificationRequestedKafkaListener {

  private final NotificationService notificationService;

  NotificationRequestedKafkaListener(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @KafkaListener(
      topics = "${app.kafka.topics.notifications-requested}",
      groupId = "${app.kafka.consumers.notification.group-id}",
      containerFactory = "notificationRequestedKafkaListenerContainerFactory")
  void onNotificationRequested(NotificationRequestedMessage message) {
    notificationService.notify(message);
  }
}

package io.github.brogowski.order.notification.service.notification;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class NotificationRequestedKafkaListener {

  private final NotificationFacade notificationFacade;

  NotificationRequestedKafkaListener(NotificationFacade notificationFacade) {
    this.notificationFacade = notificationFacade;
  }

  @KafkaListener(
      topics = "${app.kafka.topics.notifications-requested}",
      groupId = "${app.kafka.consumers.notification.group-id}",
      containerFactory = "notificationRequestedKafkaListenerContainerFactory")
  void onNotificationRequested(NotificationRequestedMessage message) {
    notificationFacade.notify(message);
  }
}

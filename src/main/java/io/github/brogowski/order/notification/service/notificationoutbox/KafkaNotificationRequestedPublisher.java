package io.github.brogowski.order.notification.service.notificationoutbox;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaNotificationRequestedPublisher implements NotificationRequestedPublisher {

  private final KafkaTemplate<String, NotificationRequestedMessage> kafkaTemplate;
  private final String topicName;
  private final Duration publishTimeout;

  KafkaNotificationRequestedPublisher(
      KafkaTemplate<String, NotificationRequestedMessage> kafkaTemplate,
      @Value("${app.kafka.topics.notifications-requested}") String topicName,
      @Value("${app.kafka.publish-timeout}") Duration publishTimeout) {
    this.kafkaTemplate = kafkaTemplate;
    this.topicName = topicName;
    this.publishTimeout = publishTimeout;
  }

  @Override
  public void publish(NotificationRequestedMessage message) {
    try {
      kafkaTemplate
          .send(topicName, message.requestId().toString(), message)
          .get(publishTimeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new NotificationOutboxPublishingException(
          "Publishing notification request was interrupted", exception);
    } catch (ExecutionException | TimeoutException exception) {
      throw new NotificationOutboxPublishingException(
          "Could not publish notification request", exception);
    }
  }
}

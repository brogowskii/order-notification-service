package io.github.brogowski.order.notification.service.notificationoutbox.domain;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import io.github.brogowski.order.notification.service.notificationoutbox.exception.NotificationOutboxPublishingException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaNotificationRequestedPublisher {

    private final KafkaTemplate<String, NotificationRequestedMessage> kafkaTemplate;
    private final String topicName;
    private final Duration publishTimeout;

    KafkaNotificationRequestedPublisher(
            KafkaTemplate<String, NotificationRequestedMessage> kafkaTemplate,
            String topicName,
            Duration publishTimeout) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
        this.publishTimeout = publishTimeout;
    }

    public void publish(NotificationRequestedMessage message) {
        try {
            kafkaTemplate
                    .send(topicName, message.shipmentNumber(), message)
                    .get(publishTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new NotificationOutboxPublishingException(
                    "Publishing notification request was interrupted", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new NotificationOutboxPublishingException("Could not publish notification request", exception);
        }
    }
}

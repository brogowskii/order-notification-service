package io.github.brogowski.order.notification.service.orderintake.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderintake.exception.OrderIntakeUnavailableException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaOrderReceivedPublisher {

    private final KafkaTemplate<String, OrderReceivedMessage> kafkaTemplate;
    private final String topicName;
    private final Duration publishTimeout;

    KafkaOrderReceivedPublisher(
            KafkaTemplate<String, OrderReceivedMessage> kafkaTemplate, String topicName, Duration publishTimeout) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
        this.publishTimeout = publishTimeout;
    }

    public void publish(OrderReceivedMessage message) {
        try {
            kafkaTemplate
                    .send(topicName, message.shipmentNumber(), message)
                    .get(publishTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OrderIntakeUnavailableException("Could not publish order request", exception);
        } catch (ExecutionException | TimeoutException | RuntimeException exception) {
            throw new OrderIntakeUnavailableException("Could not publish order request", exception);
        }
    }
}

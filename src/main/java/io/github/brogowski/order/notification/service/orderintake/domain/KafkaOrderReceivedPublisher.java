package io.github.brogowski.order.notification.service.orderintake.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderintake.exception.OrderIntakeUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaOrderReceivedPublisher implements OrderReceivedPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOrderReceivedPublisher.class);

    private final KafkaTemplate<String, OrderReceivedMessage> kafkaTemplate;
    private final String topicName;

    KafkaOrderReceivedPublisher(KafkaTemplate<String, OrderReceivedMessage> kafkaTemplate, String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void publish(OrderReceivedMessage message) {
        try {
            kafkaTemplate.send(topicName, message.shipmentNumber(), message).whenComplete((result, exception) -> {
                if (exception != null) {
                    LOGGER.error("Could not publish order request {} to Kafka", message.requestId(), exception);
                }
            });
        } catch (RuntimeException exception) {
            throw new OrderIntakeUnavailableException("Could not publish order request", exception);
        }
    }
}

package io.github.brogowski.order.notification.service.orderintake.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderintake.exception.OrderIntakeUnavailableException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class KafkaOrderReceivedPublisherTest {

    private static final String TOPIC_NAME = "orders.received.v1";
    private static final Duration PUBLISH_TIMEOUT = Duration.ofSeconds(5);

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, OrderReceivedMessage> kafkaTemplate = mock(KafkaTemplate.class);

    private final KafkaOrderReceivedPublisher publisher =
            new KafkaOrderReceivedPublisher(kafkaTemplate, TOPIC_NAME, PUBLISH_TIMEOUT);

    @Test
    void waitsUntilKafkaAcceptsMessage() {
        OrderReceivedMessage message = message();
        when(kafkaTemplate.send(TOPIC_NAME, message.shipmentNumber(), message))
                .thenReturn(CompletableFuture.completedFuture(null));

        publisher.publish(message);

        verify(kafkaTemplate).send(TOPIC_NAME, message.shipmentNumber(), message);
    }

    @Test
    void rejectsOrderWhenKafkaPublishFails() {
        OrderReceivedMessage message = message();
        when(kafkaTemplate.send(TOPIC_NAME, message.shipmentNumber(), message))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Kafka unavailable")));

        assertThatThrownBy(() -> publisher.publish(message))
                .isInstanceOf(OrderIntakeUnavailableException.class)
                .hasMessage("Could not publish order request");
    }

    private static OrderReceivedMessage message() {
        return new OrderReceivedMessage(
                UUID.randomUUID(),
                "PL123456789",
                "recipient@example.com",
                "PL",
                "DE",
                42,
                Instant.parse("2026-05-20T16:30:00Z"));
    }
}

package io.github.brogowski.order.notification.service.orderintake.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderAcceptedDto;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderRequestDto;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrderIntakeFacadeTest {

    private static final Instant NOW = Instant.parse("2026-05-20T16:30:00Z");

    @Test
    void acceptsOrderRequestAndPublishesMessage() {
        KafkaOrderReceivedPublisher publisher = mock(KafkaOrderReceivedPublisher.class);
        OrderIntakeFacade facade = new OrderIntakeFacade(publisher, Clock.fixed(NOW, ZoneOffset.UTC));

        OrderAcceptedDto accepted =
                facade.accept(new OrderRequestDto("PL123456789", "recipient@example.com", "PL", "DE", 42));

        ArgumentCaptor<OrderReceivedMessage> messageCaptor = ArgumentCaptor.forClass(OrderReceivedMessage.class);
        verify(publisher).publish(messageCaptor.capture());
        OrderReceivedMessage publishedMessage = messageCaptor.getValue();

        assertThat(accepted.requestId()).isNotNull();
        assertThat(accepted.acceptedAt()).isEqualTo(NOW);
        assertThat(publishedMessage.requestId()).isEqualTo(accepted.requestId());
        assertThat(publishedMessage.shipmentNumber()).isEqualTo("PL123456789");
        assertThat(publishedMessage.recipientEmail()).isEqualTo("recipient@example.com");
        assertThat(publishedMessage.recipientCountryCode()).isEqualTo("PL");
        assertThat(publishedMessage.senderCountryCode()).isEqualTo("DE");
        assertThat(publishedMessage.statusCode()).isEqualTo(42);
        assertThat(publishedMessage.receivedAt()).isEqualTo(NOW);
    }
}

package io.github.brogowski.order.notification.service.orderintake.domain;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderAcceptedDto;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderRequestDto;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class OrderIntakeFacadeTest {

  private static final Instant NOW = Instant.parse("2026-05-20T16:30:00Z");

  @Test
  void acceptsOrderRequestAndPublishesMessage() {
    CapturingOrderReceivedPublisher publisher = new CapturingOrderReceivedPublisher();
    OrderIntakeFacade facade =
        new OrderIntakeFacade(publisher, Clock.fixed(NOW, ZoneOffset.UTC));

    OrderAcceptedDto accepted =
        facade.accept(
            new OrderRequestDto(
                "PL123456789", "recipient@example.com", "PL", "DE", 42));

    assertThat(accepted.requestId()).isNotNull();
    assertThat(accepted.acceptedAt()).isEqualTo(NOW);
    assertThat(publisher.publishedMessage).isNotNull();
    assertThat(publisher.publishedMessage.requestId()).isEqualTo(accepted.requestId());
    assertThat(publisher.publishedMessage.shipmentNumber()).isEqualTo("PL123456789");
    assertThat(publisher.publishedMessage.recipientEmail()).isEqualTo("recipient@example.com");
    assertThat(publisher.publishedMessage.recipientCountryCode()).isEqualTo("PL");
    assertThat(publisher.publishedMessage.senderCountryCode()).isEqualTo("DE");
    assertThat(publisher.publishedMessage.statusCode()).isEqualTo(42);
    assertThat(publisher.publishedMessage.receivedAt()).isEqualTo(NOW);
  }

  private static class CapturingOrderReceivedPublisher implements OrderReceivedPublisher {

    private OrderReceivedMessage publishedMessage;

    @Override
    public void publish(OrderReceivedMessage message) {
      this.publishedMessage = message;
    }
  }
}

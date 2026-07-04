package io.github.brogowski.order.notification.service.orderintake.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderAcceptedDto;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderRequestDto;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class OrderIntakeFacade {

  private final OrderReceivedPublisher orderReceivedPublisher;
  private final Clock clock;

  OrderIntakeFacade(OrderReceivedPublisher orderReceivedPublisher, Clock clock) {
    this.orderReceivedPublisher = orderReceivedPublisher;
    this.clock = clock;
  }

  public OrderAcceptedDto accept(OrderRequestDto request) {
    IncomingOrderRequest incomingOrderRequest = IncomingOrderRequest.from(request);
    UUID requestId = UUID.randomUUID();
    Instant acceptedAt = Instant.now(clock);

    orderReceivedPublisher.publish(
        new OrderReceivedMessage(
            requestId,
            incomingOrderRequest.shipmentNumber().value(),
            incomingOrderRequest.recipientEmail().value(),
            incomingOrderRequest.recipientCountryCode().value(),
            incomingOrderRequest.senderCountryCode().value(),
            incomingOrderRequest.statusCode().value(),
            acceptedAt));

    return new OrderAcceptedDto(requestId, acceptedAt);
  }
}

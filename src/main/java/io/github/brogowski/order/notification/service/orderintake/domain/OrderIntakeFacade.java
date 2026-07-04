package io.github.brogowski.order.notification.service.orderintake.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderAcceptedDto;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderRequestDto;
import io.github.brogowski.order.notification.service.orderintake.exception.OrderIntakeRateLimitExceededException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrderIntakeFacade {

  private final OrderReceivedPublisher orderReceivedPublisher;
  private final OrderIntakeRateLimiter orderIntakeRateLimiter;
  private final Clock clock;

  OrderIntakeFacade(
      OrderReceivedPublisher orderReceivedPublisher,
      OrderIntakeRateLimiter orderIntakeRateLimiter,
      Clock clock) {
    this.orderReceivedPublisher = orderReceivedPublisher;
    this.orderIntakeRateLimiter = orderIntakeRateLimiter;
    this.clock = clock;
  }

  public OrderAcceptedDto accept(OrderRequestDto request) {
    if (!orderIntakeRateLimiter.tryAcquire()) {
      throw new OrderIntakeRateLimitExceededException("Order intake rate limit exceeded");
    }
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

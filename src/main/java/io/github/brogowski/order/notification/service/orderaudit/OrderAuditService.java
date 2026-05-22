package io.github.brogowski.order.notification.service.orderaudit;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.notificationoutbox.NotificationOutboxCommand;
import io.github.brogowski.order.notification.service.notificationoutbox.NotificationOutboxFacade;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class OrderAuditService implements OrderAuditFacade {

  private final OrderRequestAuditRepository orderRequestAuditRepository;
  private final NotificationOutboxFacade notificationOutboxFacade;
  private final Clock clock;

  OrderAuditService(
      OrderRequestAuditRepository orderRequestAuditRepository,
      NotificationOutboxFacade notificationOutboxFacade,
      Clock clock) {
    this.orderRequestAuditRepository = orderRequestAuditRepository;
    this.notificationOutboxFacade = notificationOutboxFacade;
    this.clock = clock;
  }

  @Transactional
  void audit(OrderReceivedMessage message) {
    Instant storedAt = Instant.now(clock);
    OrderRequestAudit audit = OrderRequestAudit.from(message, storedAt);

    orderRequestAuditRepository.save(audit);
    notificationOutboxFacade.schedule(
        new NotificationOutboxCommand(
            audit.requestId(),
            audit.shipmentNumber(),
            audit.recipientEmail(),
            audit.recipientCountryCode(),
            audit.senderCountryCode(),
            audit.statusCode(),
            storedAt));
  }

  @Override
  public Optional<OrderRequestAuditDto> findByRequestId(UUID requestId) {
    return orderRequestAuditRepository.findByRequestId(requestId)
        .map(OrderRequestAudit::toDto);
  }
}

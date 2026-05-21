package io.github.brogowski.order.notification.service.orderaudit;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class OrderAuditService implements OrderAuditFacade {

  private final OrderRequestAuditRepository orderRequestAuditRepository;
  private final NotificationOutboxRepository notificationOutboxRepository;
  private final Clock clock;

  OrderAuditService(
      OrderRequestAuditRepository orderRequestAuditRepository,
      NotificationOutboxRepository notificationOutboxRepository,
      Clock clock) {
    this.orderRequestAuditRepository = orderRequestAuditRepository;
    this.notificationOutboxRepository = notificationOutboxRepository;
    this.clock = clock;
  }

  @Transactional
  void audit(OrderReceivedMessage message) {
    Instant storedAt = Instant.now(clock);
    OrderRequestAudit audit = OrderRequestAudit.from(message, storedAt);

    orderRequestAuditRepository.save(audit);
    notificationOutboxRepository.save(NotificationOutboxEntry.from(audit, storedAt));
  }

  @Override
  public Optional<OrderRequestAuditDto> findByRequestId(UUID requestId) {
    return orderRequestAuditRepository.findByRequestId(requestId).map(OrderRequestAudit::toDto);
  }
}

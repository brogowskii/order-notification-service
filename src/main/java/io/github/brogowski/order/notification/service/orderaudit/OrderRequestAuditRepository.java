package io.github.brogowski.order.notification.service.orderaudit;

import java.util.Optional;
import java.util.UUID;

interface OrderRequestAuditRepository {

  void save(OrderRequestAudit audit);

  Optional<OrderRequestAudit> findByRequestId(UUID requestId);
}

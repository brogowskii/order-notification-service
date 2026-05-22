package io.github.brogowski.order.notification.service.orderaudit;

import java.util.Optional;
import java.util.UUID;

public interface OrderAuditFacade {

  Optional<OrderRequestAuditDto> findByRequestId(UUID requestId);
}

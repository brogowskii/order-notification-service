package io.github.brogowski.order.notification.service.orderaudit.web;

import io.github.brogowski.order.notification.service.orderaudit.domain.OrderAuditFacade;
import io.github.brogowski.order.notification.service.orderaudit.dto.OrderRequestAuditDto;
import io.github.brogowski.order.notification.service.orderaudit.exception.OrderRequestAuditNotFoundException;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order-requests")
class OrderAuditController {

  private final OrderAuditFacade orderAuditFacade;

  OrderAuditController(OrderAuditFacade orderAuditFacade) {
    this.orderAuditFacade = orderAuditFacade;
  }

  @GetMapping("/{requestId}")
  OrderRequestAuditDto findByRequestId(@PathVariable UUID requestId) {
    return orderAuditFacade
        .findByRequestId(requestId)
        .orElseThrow(() -> new OrderRequestAuditNotFoundException(requestId));
  }
}

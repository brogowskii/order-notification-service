package io.github.brogowski.order.notification.service.orderaudit;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

  @ResponseStatus(HttpStatus.NOT_FOUND)
  private static class OrderRequestAuditNotFoundException extends RuntimeException {

    OrderRequestAuditNotFoundException(UUID requestId) {
      super("Order request audit not found: " + requestId);
    }
  }
}

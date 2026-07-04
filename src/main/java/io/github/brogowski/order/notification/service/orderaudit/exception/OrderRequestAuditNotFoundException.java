package io.github.brogowski.order.notification.service.orderaudit.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderRequestAuditNotFoundException extends RuntimeException {

  public OrderRequestAuditNotFoundException(UUID requestId) {
    super("Order request audit not found: " + requestId);
  }
}

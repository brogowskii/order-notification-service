package io.github.brogowski.order.notification.service.notification.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotificationLogNotFoundException extends RuntimeException {

  public NotificationLogNotFoundException(UUID requestId) {
    super("Notification log not found: " + requestId);
  }
}

package io.github.brogowski.order.notification.service.notification;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController {

  private final NotificationFacade notificationFacade;

  NotificationController(NotificationFacade notificationFacade) {
    this.notificationFacade = notificationFacade;
  }

  @GetMapping("/{requestId}")
  NotificationLogDto findByRequestId(@PathVariable UUID requestId) {
    return notificationFacade
        .findByRequestId(requestId)
        .orElseThrow(() -> new NotificationLogNotFoundException(requestId));
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  private static class NotificationLogNotFoundException extends RuntimeException {

    NotificationLogNotFoundException(UUID requestId) {
      super("Notification log not found: " + requestId);
    }
  }
}

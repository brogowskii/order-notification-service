package io.github.brogowski.order.notification.service.notification.web;

import io.github.brogowski.order.notification.service.notification.domain.NotificationFacade;
import io.github.brogowski.order.notification.service.notification.dto.NotificationLogDto;
import io.github.brogowski.order.notification.service.notification.exception.NotificationLogNotFoundException;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}

package io.github.brogowski.order.notification.service.notification;

import java.util.Optional;
import java.util.UUID;

public interface NotificationFacade {

  Optional<NotificationLogDto> findByRequestId(UUID requestId);
}

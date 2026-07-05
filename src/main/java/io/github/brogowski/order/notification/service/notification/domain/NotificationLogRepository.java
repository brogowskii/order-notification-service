package io.github.brogowski.order.notification.service.notification.domain;

import java.util.Optional;
import java.util.UUID;

interface NotificationLogRepository {

    void save(NotificationLog log);

    Optional<NotificationLog> findByRequestId(UUID requestId);
}

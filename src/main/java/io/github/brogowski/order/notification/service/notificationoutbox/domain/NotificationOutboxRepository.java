package io.github.brogowski.order.notification.service.notificationoutbox.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface NotificationOutboxRepository {

    void save(NotificationOutboxEntry entry);

    List<NotificationOutboxEntry> claimPending(Instant now, int limit);

    void markPublished(UUID id, Instant publishedAt);

    void markFailed(UUID id, Instant nextAttemptAt, int maxAttempts);
}

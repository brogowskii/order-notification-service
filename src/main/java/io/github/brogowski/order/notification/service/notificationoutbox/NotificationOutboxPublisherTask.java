package io.github.brogowski.order.notification.service.notificationoutbox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "app.notification-outbox.enabled",
    havingValue = "true",
    matchIfMissing = true)
class NotificationOutboxPublisherTask {

  private final NotificationOutboxRepository notificationOutboxRepository;
  private final NotificationRequestedPublisher notificationRequestedPublisher;
  private final Clock clock;
  private final int batchSize;
  private final Duration retryDelay;

  NotificationOutboxPublisherTask(
      NotificationOutboxRepository notificationOutboxRepository,
      NotificationRequestedPublisher notificationRequestedPublisher,
      Clock clock,
      @Value("${app.notification-outbox.batch-size}") int batchSize,
      @Value("${app.notification-outbox.retry-delay}") Duration retryDelay) {
    this.notificationOutboxRepository = notificationOutboxRepository;
    this.notificationRequestedPublisher = notificationRequestedPublisher;
    this.clock = clock;
    this.batchSize = batchSize;
    this.retryDelay = retryDelay;
  }

  @Scheduled(fixedDelayString = "${app.notification-outbox.publish-interval}")
  void publishPending() {
    Instant now = Instant.now(clock);
    List<NotificationOutboxEntry> entries = notificationOutboxRepository.findPending(now, batchSize);

    for (NotificationOutboxEntry entry : entries) {
      publish(entry);
    }
  }

  private void publish(NotificationOutboxEntry entry) {
    try {
      notificationRequestedPublisher.publish(entry.toMessage());
      notificationOutboxRepository.markPublished(entry.id(), Instant.now(clock));
    } catch (NotificationOutboxPublishingException exception) {
      notificationOutboxRepository.markFailed(entry.id(), Instant.now(clock).plus(retryDelay));
    }
  }
}

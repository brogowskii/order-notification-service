package io.github.brogowski.order.notification.service.notificationoutbox.domain;

import io.github.brogowski.order.notification.service.notificationoutbox.exception.NotificationOutboxPublishingException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;

class NotificationOutboxPublisherTask {

  private final NotificationOutboxRepository notificationOutboxRepository;
  private final NotificationRequestedPublisher notificationRequestedPublisher;
  private final Clock clock;
  private final int batchSize;
  private final Duration retryDelay;
  private final int maxAttempts;

  NotificationOutboxPublisherTask(
      NotificationOutboxRepository notificationOutboxRepository,
      NotificationRequestedPublisher notificationRequestedPublisher,
      Clock clock,
      int batchSize,
      Duration retryDelay,
      int maxAttempts) {
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("Notification outbox max attempts must be positive");
    }
    this.notificationOutboxRepository = notificationOutboxRepository;
    this.notificationRequestedPublisher = notificationRequestedPublisher;
    this.clock = clock;
    this.batchSize = batchSize;
    this.retryDelay = retryDelay;
    this.maxAttempts = maxAttempts;
  }

  @Scheduled(fixedDelayString = "${app.notification-outbox.publish-interval}")
  void publishPending() {
    Instant now = Instant.now(clock);
    List<NotificationOutboxEntry> entries = notificationOutboxRepository.claimPending(now, batchSize);

    for (NotificationOutboxEntry entry : entries) {
      publish(entry);
    }
  }

  private void publish(NotificationOutboxEntry entry) {
    try {
      notificationRequestedPublisher.publish(entry.toMessage());
      notificationOutboxRepository.markPublished(entry.id(), Instant.now(clock));
    } catch (NotificationOutboxPublishingException exception) {
      notificationOutboxRepository.markFailed(
          entry.id(), Instant.now(clock).plus(retryDelay), maxAttempts);
    }
  }
}

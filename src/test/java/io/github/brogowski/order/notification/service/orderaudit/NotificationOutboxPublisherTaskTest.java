package io.github.brogowski.order.notification.service.orderaudit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationOutboxPublisherTaskTest {

  private static final Instant NOW = Instant.parse("2026-05-21T10:00:00Z");

  @Test
  void publishesPendingOutboxEntriesAndMarksThemPublished() {
    InMemoryNotificationOutboxRepository repository =
        new InMemoryNotificationOutboxRepository(
            List.of(
                new NotificationOutboxEntry(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "PL123456789",
                    "recipient@example.com",
                    "PL",
                    "DE",
                    42,
                    NOW,
                    OutboxStatus.PENDING,
                    0,
                    NOW,
                    NOW,
                    null)));
    CapturingNotificationRequestedPublisher publisher =
        new CapturingNotificationRequestedPublisher(false);
    NotificationOutboxPublisherTask task =
        new NotificationOutboxPublisherTask(
            repository, publisher, fixedClock(), 10, Duration.ofSeconds(10));

    task.publishPending();

    assertThat(publisher.publishedMessages).hasSize(1);
    NotificationRequestedMessage message = publisher.publishedMessages.get(0);
    assertThat(message.shipmentNumber()).isEqualTo("PL123456789");
    assertThat(message.recipientEmail()).isEqualTo("recipient@example.com");
    assertThat(repository.publishedEntryId).isEqualTo(repository.entries.get(0).id());
    assertThat(repository.publishedAt).isEqualTo(NOW);
  }

  @Test
  void schedulesRetryWhenPublishingFails() {
    InMemoryNotificationOutboxRepository repository =
        new InMemoryNotificationOutboxRepository(
            List.of(
                new NotificationOutboxEntry(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "PL123456789",
                    "recipient@example.com",
                    "PL",
                    "DE",
                    42,
                    NOW,
                    OutboxStatus.PENDING,
                    0,
                    NOW,
                    NOW,
                    null)));
    NotificationOutboxPublisherTask task =
        new NotificationOutboxPublisherTask(
            repository,
            new CapturingNotificationRequestedPublisher(true),
            fixedClock(),
            10,
            Duration.ofSeconds(10));

    task.publishPending();

    assertThat(repository.failedEntryId).isEqualTo(repository.entries.get(0).id());
    assertThat(repository.nextAttemptAt).isEqualTo(NOW.plusSeconds(10));
  }

  private static Clock fixedClock() {
    return Clock.fixed(NOW, ZoneOffset.UTC);
  }

  private static class InMemoryNotificationOutboxRepository
      implements NotificationOutboxRepository {

    private final List<NotificationOutboxEntry> entries;
    private UUID publishedEntryId;
    private Instant publishedAt;
    private UUID failedEntryId;
    private Instant nextAttemptAt;

    private InMemoryNotificationOutboxRepository(List<NotificationOutboxEntry> entries) {
      this.entries = new ArrayList<>(entries);
    }

    @Override
    public void save(NotificationOutboxEntry entry) {
      entries.add(entry);
    }

    @Override
    public List<NotificationOutboxEntry> findPending(Instant now, int limit) {
      return entries.stream()
          .filter(entry -> entry.status() == OutboxStatus.PENDING)
          .filter(entry -> !entry.nextAttemptAt().isAfter(now))
          .limit(limit)
          .toList();
    }

    @Override
    public void markPublished(UUID id, Instant publishedAt) {
      this.publishedEntryId = id;
      this.publishedAt = publishedAt;
    }

    @Override
    public void markFailed(UUID id, Instant nextAttemptAt) {
      this.failedEntryId = id;
      this.nextAttemptAt = nextAttemptAt;
    }
  }

  private static class CapturingNotificationRequestedPublisher
      implements NotificationRequestedPublisher {

    private final boolean fail;
    private final List<NotificationRequestedMessage> publishedMessages = new ArrayList<>();

    private CapturingNotificationRequestedPublisher(boolean fail) {
      this.fail = fail;
    }

    @Override
    public void publish(NotificationRequestedMessage message) {
      if (fail) {
        throw new NotificationOutboxPublishingException("fail", new RuntimeException("fail"));
      }
      publishedMessages.add(message);
    }
  }
}

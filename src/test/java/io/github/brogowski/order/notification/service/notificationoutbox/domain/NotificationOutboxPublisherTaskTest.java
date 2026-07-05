package io.github.brogowski.order.notification.service.notificationoutbox.domain;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import io.github.brogowski.order.notification.service.notificationoutbox.exception.NotificationOutboxPublishingException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class NotificationOutboxPublisherTaskTest {

    private static final Instant NOW = Instant.parse("2026-05-21T10:00:00Z");
    private static final Duration RETRY_DELAY = Duration.ofSeconds(10);
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(1);

    @Test
    void publishesPendingOutboxEntriesAndMarksThemPublished() {
        InMemoryNotificationOutboxRepository repository =
                new InMemoryNotificationOutboxRepository(List.of(new NotificationOutboxEntry(
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
                        null,
                        null)));
        CapturingNotificationRequestedPublisher publisher = new CapturingNotificationRequestedPublisher(false);
        NotificationOutboxPublisherTask task = new NotificationOutboxPublisherTask(
                repository, publisher, fixedClock(), 10, RETRY_DELAY, PROCESSING_TIMEOUT, 3);

        task.publishPending();

        assertThat(publisher.publishedMessages).hasSize(1);
        NotificationRequestedMessage message = publisher.publishedMessages.get(0);
        assertThat(message.shipmentNumber()).isEqualTo("PL123456789");
        assertThat(message.recipientEmail()).isEqualTo("recipient@example.com");
        assertThat(repository.publishedEntryId)
                .isEqualTo(repository.entries.get(0).id());
        assertThat(repository.publishedAt).isEqualTo(NOW);
        assertThat(repository.entries.get(0).status()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    void schedulesRetryWhenPublishingFails() {
        InMemoryNotificationOutboxRepository repository =
                new InMemoryNotificationOutboxRepository(List.of(new NotificationOutboxEntry(
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
                        null,
                        null)));
        NotificationOutboxPublisherTask task = new NotificationOutboxPublisherTask(
                repository,
                new CapturingNotificationRequestedPublisher(true),
                fixedClock(),
                10,
                RETRY_DELAY,
                PROCESSING_TIMEOUT,
                3);

        task.publishPending();

        assertThat(repository.failedEntryId).isEqualTo(repository.entries.get(0).id());
        assertThat(repository.nextAttemptAt).isEqualTo(NOW.plusSeconds(10));
        assertThat(repository.failedMaxAttempts).isEqualTo(3);
        assertThat(repository.entries.get(0).status()).isEqualTo(OutboxStatus.PENDING);
        assertThat(repository.entries.get(0).attempts()).isEqualTo(1);
    }

    @Test
    void marksEntryFailedAfterMaxAttempts() {
        UUID outboxId = UUID.randomUUID();
        InMemoryNotificationOutboxRepository repository =
                new InMemoryNotificationOutboxRepository(List.of(new NotificationOutboxEntry(
                        outboxId,
                        UUID.randomUUID(),
                        "PL123456789",
                        "recipient@example.com",
                        "PL",
                        "DE",
                        42,
                        NOW,
                        OutboxStatus.PENDING,
                        2,
                        NOW,
                        NOW,
                        null,
                        null)));
        NotificationOutboxPublisherTask task = new NotificationOutboxPublisherTask(
                repository,
                new CapturingNotificationRequestedPublisher(true),
                fixedClock(),
                10,
                RETRY_DELAY,
                PROCESSING_TIMEOUT,
                3);

        task.publishPending();

        assertThat(repository.entries.get(0).status()).isEqualTo(OutboxStatus.FAILED);
        assertThat(repository.entries.get(0).attempts()).isEqualTo(3);
    }

    @Test
    void skipsFreshlyClaimedOutboxEntries() {
        InMemoryNotificationOutboxRepository repository =
                new InMemoryNotificationOutboxRepository(List.of(new NotificationOutboxEntry(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "PL123456789",
                        "recipient@example.com",
                        "PL",
                        "DE",
                        42,
                        NOW,
                        OutboxStatus.PROCESSING,
                        0,
                        NOW,
                        NOW,
                        NOW,
                        null)));
        CapturingNotificationRequestedPublisher publisher = new CapturingNotificationRequestedPublisher(false);
        NotificationOutboxPublisherTask task = new NotificationOutboxPublisherTask(
                repository, publisher, fixedClock(), 10, RETRY_DELAY, PROCESSING_TIMEOUT, 3);

        task.publishPending();

        assertThat(publisher.publishedMessages).isEmpty();
    }

    @Test
    void reclaimsStaleProcessingOutboxEntries() {
        InMemoryNotificationOutboxRepository repository =
                new InMemoryNotificationOutboxRepository(List.of(new NotificationOutboxEntry(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "PL123456789",
                        "recipient@example.com",
                        "PL",
                        "DE",
                        42,
                        NOW,
                        OutboxStatus.PROCESSING,
                        0,
                        NOW,
                        NOW,
                        NOW.minus(PROCESSING_TIMEOUT).minusSeconds(1),
                        null)));
        CapturingNotificationRequestedPublisher publisher = new CapturingNotificationRequestedPublisher(false);
        NotificationOutboxPublisherTask task = new NotificationOutboxPublisherTask(
                repository, publisher, fixedClock(), 10, RETRY_DELAY, PROCESSING_TIMEOUT, 3);

        task.publishPending();

        assertThat(publisher.publishedMessages).hasSize(1);
        assertThat(repository.publishedEntryId)
                .isEqualTo(repository.entries.get(0).id());
    }

    private static Clock fixedClock() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }

    private static class InMemoryNotificationOutboxRepository implements NotificationOutboxRepository {

        private final List<NotificationOutboxEntry> entries;
        private UUID publishedEntryId;
        private Instant publishedAt;
        private UUID failedEntryId;
        private Instant nextAttemptAt;
        private int failedMaxAttempts;

        private InMemoryNotificationOutboxRepository(List<NotificationOutboxEntry> entries) {
            this.entries = new ArrayList<>(entries);
        }

        @Override
        public void save(NotificationOutboxEntry entry) {
            entries.add(entry);
        }

        @Override
        public List<NotificationOutboxEntry> claimPending(Instant now, Instant processingExpiredBefore, int limit) {
            List<NotificationOutboxEntry> claimedEntries = new ArrayList<>();
            for (NotificationOutboxEntry entry : entries) {
                if (claimedEntries.size() == limit) {
                    break;
                }
                if (!isClaimable(entry, now, processingExpiredBefore)) {
                    continue;
                }
                NotificationOutboxEntry claimed = new NotificationOutboxEntry(
                        entry.id(),
                        entry.requestId(),
                        entry.shipmentNumber(),
                        entry.recipientEmail(),
                        entry.recipientCountryCode(),
                        entry.senderCountryCode(),
                        entry.statusCode(),
                        entry.requestedAt(),
                        OutboxStatus.PROCESSING,
                        entry.attempts(),
                        entry.createdAt(),
                        entry.nextAttemptAt(),
                        now,
                        entry.publishedAt());
                replace(entry.id(), ignored -> claimed);
                claimedEntries.add(claimed);
            }
            return claimedEntries;
        }

        private static boolean isClaimable(
                NotificationOutboxEntry entry, Instant now, Instant processingExpiredBefore) {
            if (entry.status() == OutboxStatus.PENDING) {
                return !entry.nextAttemptAt().isAfter(now);
            }
            if (entry.status() == OutboxStatus.PROCESSING) {
                return entry.claimedAt() == null || !entry.claimedAt().isAfter(processingExpiredBefore);
            }
            return false;
        }

        @Override
        public void markPublished(UUID id, Instant publishedAt) {
            this.publishedEntryId = id;
            this.publishedAt = publishedAt;
            replace(
                    id,
                    entry -> new NotificationOutboxEntry(
                            entry.id(),
                            entry.requestId(),
                            entry.shipmentNumber(),
                            entry.recipientEmail(),
                            entry.recipientCountryCode(),
                            entry.senderCountryCode(),
                            entry.statusCode(),
                            entry.requestedAt(),
                            OutboxStatus.PUBLISHED,
                            entry.attempts(),
                            entry.createdAt(),
                            entry.nextAttemptAt(),
                            null,
                            publishedAt));
        }

        @Override
        public void markFailed(UUID id, Instant nextAttemptAt, int maxAttempts) {
            this.failedEntryId = id;
            this.nextAttemptAt = nextAttemptAt;
            this.failedMaxAttempts = maxAttempts;
            replace(id, entry -> {
                int attempts = entry.attempts() + 1;
                OutboxStatus status = attempts >= maxAttempts ? OutboxStatus.FAILED : OutboxStatus.PENDING;
                return new NotificationOutboxEntry(
                        entry.id(),
                        entry.requestId(),
                        entry.shipmentNumber(),
                        entry.recipientEmail(),
                        entry.recipientCountryCode(),
                        entry.senderCountryCode(),
                        entry.statusCode(),
                        entry.requestedAt(),
                        status,
                        attempts,
                        entry.createdAt(),
                        nextAttemptAt,
                        null,
                        entry.publishedAt());
            });
        }

        private void replace(UUID id, Function<NotificationOutboxEntry, NotificationOutboxEntry> mapper) {
            for (int index = 0; index < entries.size(); index++) {
                NotificationOutboxEntry entry = entries.get(index);
                if (entry.id().equals(id)) {
                    entries.set(index, mapper.apply(entry));
                    return;
                }
            }
        }
    }

    private static class CapturingNotificationRequestedPublisher implements NotificationRequestedPublisher {

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

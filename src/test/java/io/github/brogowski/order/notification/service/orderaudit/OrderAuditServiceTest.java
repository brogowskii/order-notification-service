package io.github.brogowski.order.notification.service.orderaudit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderAuditServiceTest {

  private static final Instant RECEIVED_AT = Instant.parse("2026-05-21T10:00:00Z");
  private static final Instant STORED_AT = Instant.parse("2026-05-21T10:00:01Z");

  @Test
  void storesAuditAndCreatesNotificationOutboxEntry() {
    InMemoryOrderRequestAuditRepository auditRepository = new InMemoryOrderRequestAuditRepository();
    CapturingNotificationOutboxRepository outboxRepository =
        new CapturingNotificationOutboxRepository();
    OrderAuditService service =
        new OrderAuditService(auditRepository, outboxRepository, fixedClock());

    UUID requestId = UUID.randomUUID();
    service.audit(
        new OrderReceivedMessage(
            requestId,
            "PL123456789",
            "recipient@example.com",
            "PL",
            "DE",
            42,
            RECEIVED_AT));

    OrderRequestAudit audit = auditRepository.savedAudit;
    assertThat(audit.requestId()).isEqualTo(requestId);
    assertThat(audit.shipmentNumber()).isEqualTo("PL123456789");
    assertThat(audit.recipientEmail()).isEqualTo("recipient@example.com");
    assertThat(audit.recipientCountryCode()).isEqualTo("PL");
    assertThat(audit.senderCountryCode()).isEqualTo("DE");
    assertThat(audit.statusCode()).isEqualTo(42);
    assertThat(audit.receivedAt()).isEqualTo(RECEIVED_AT);
    assertThat(audit.storedAt()).isEqualTo(STORED_AT);

    NotificationOutboxEntry outboxEntry = outboxRepository.savedEntry;
    assertThat(outboxEntry.id()).isNotNull();
    assertThat(outboxEntry.requestId()).isEqualTo(requestId);
    assertThat(outboxEntry.status()).isEqualTo(OutboxStatus.PENDING);
    assertThat(outboxEntry.attempts()).isZero();
    assertThat(outboxEntry.createdAt()).isEqualTo(STORED_AT);
    assertThat(outboxEntry.nextAttemptAt()).isEqualTo(STORED_AT);
    assertThat(outboxEntry.publishedAt()).isNull();
  }

  @Test
  void returnsAuditDtoByRequestId() {
    UUID requestId = UUID.randomUUID();
    InMemoryOrderRequestAuditRepository auditRepository = new InMemoryOrderRequestAuditRepository();
    auditRepository.savedAudit =
        new OrderRequestAudit(
            requestId,
            "PL123456789",
            "recipient@example.com",
            "PL",
            "DE",
            42,
            RECEIVED_AT,
            STORED_AT);
    OrderAuditService service =
        new OrderAuditService(
            auditRepository, new CapturingNotificationOutboxRepository(), fixedClock());

    Optional<OrderRequestAuditDto> audit = service.findByRequestId(requestId);

    assertThat(audit).isPresent();
    assertThat(audit.orElseThrow().requestId()).isEqualTo(requestId);
    assertThat(audit.orElseThrow().receivedAt()).isEqualTo(RECEIVED_AT);
    assertThat(audit.orElseThrow().storedAt()).isEqualTo(STORED_AT);
  }

  private static Clock fixedClock() {
    return Clock.fixed(STORED_AT, ZoneOffset.UTC);
  }

  private static class InMemoryOrderRequestAuditRepository implements OrderRequestAuditRepository {

    private OrderRequestAudit savedAudit;

    @Override
    public void save(OrderRequestAudit audit) {
      this.savedAudit = audit;
    }

    @Override
    public Optional<OrderRequestAudit> findByRequestId(UUID requestId) {
      return Optional.ofNullable(savedAudit).filter(audit -> audit.requestId().equals(requestId));
    }
  }

  private static class CapturingNotificationOutboxRepository
      implements NotificationOutboxRepository {

    private NotificationOutboxEntry savedEntry;

    @Override
    public void save(NotificationOutboxEntry entry) {
      this.savedEntry = entry;
    }

    @Override
    public List<NotificationOutboxEntry> findPending(Instant now, int limit) {
      return List.of();
    }

    @Override
    public void markPublished(UUID id, Instant publishedAt) {}

    @Override
    public void markFailed(UUID id, Instant nextAttemptAt) {}
  }
}

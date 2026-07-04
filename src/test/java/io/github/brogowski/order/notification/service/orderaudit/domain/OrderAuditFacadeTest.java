package io.github.brogowski.order.notification.service.orderaudit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.notificationoutbox.domain.NotificationOutboxCommand;
import io.github.brogowski.order.notification.service.notificationoutbox.domain.NotificationOutboxFacade;
import io.github.brogowski.order.notification.service.orderaudit.dto.OrderRequestAuditDto;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrderAuditFacadeTest {

  private static final Instant RECEIVED_AT = Instant.parse("2026-05-21T10:00:00Z");
  private static final Instant STORED_AT = Instant.parse("2026-05-21T10:00:01Z");

  @Test
  void storesAuditAndCreatesNotificationOutboxEntry() {
    InMemoryOrderRequestAuditRepository auditRepository = new InMemoryOrderRequestAuditRepository();
    NotificationOutboxFacade outboxFacade = mock(NotificationOutboxFacade.class);
    OrderAuditFacade facade =
        new OrderAuditFacade(auditRepository, outboxFacade, fixedClock());

    UUID requestId = UUID.randomUUID();
    facade.audit(
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

    ArgumentCaptor<NotificationOutboxCommand> commandCaptor =
        ArgumentCaptor.forClass(NotificationOutboxCommand.class);
    verify(outboxFacade).schedule(commandCaptor.capture());
    NotificationOutboxCommand command = commandCaptor.getValue();
    assertThat(command.requestId()).isEqualTo(requestId);
    assertThat(command.shipmentNumber()).isEqualTo("PL123456789");
    assertThat(command.recipientEmail()).isEqualTo("recipient@example.com");
    assertThat(command.recipientCountryCode()).isEqualTo("PL");
    assertThat(command.senderCountryCode()).isEqualTo("DE");
    assertThat(command.statusCode()).isEqualTo(42);
    assertThat(command.requestedAt()).isEqualTo(STORED_AT);
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
    OrderAuditFacade facade =
        new OrderAuditFacade(
            auditRepository, mock(NotificationOutboxFacade.class), fixedClock());

    Optional<OrderRequestAuditDto> audit = facade.findByRequestId(requestId);

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
}

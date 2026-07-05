package io.github.brogowski.order.notification.service.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import io.github.brogowski.order.notification.service.notification.dto.NotificationLogDto;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationFacadeTest {

    private static final Instant REQUESTED_AT = Instant.parse("2026-05-21T10:00:00Z");
    private static final Instant SENT_AT = Instant.parse("2026-05-21T10:00:01Z");

    @Test
    void sendsMockEmailAndStoresNotificationLog() {
        CapturingEmailSender emailSender = new CapturingEmailSender();
        InMemoryNotificationLogRepository notificationLogRepository = new InMemoryNotificationLogRepository();
        NotificationFacade facade = new NotificationFacade(
                new EmailMessageFactory(),
                emailSender,
                notificationLogRepository,
                Clock.fixed(SENT_AT, ZoneOffset.UTC));

        UUID requestId = UUID.randomUUID();
        facade.notify(new NotificationRequestedMessage(
                requestId, "PL123456789", "recipient@example.com", "PL", "DE", 42, REQUESTED_AT));

        assertThat(emailSender.sentMessage.recipientEmail()).isEqualTo("recipient@example.com");
        assertThat(emailSender.sentMessage.subject()).isEqualTo("Shipment PL123456789 status update");
        assertThat(emailSender.sentMessage.body()).contains("Shipment number: PL123456789");
        assertThat(emailSender.sentMessage.body()).contains("Status code: 42");

        NotificationLog savedLog = notificationLogRepository.savedLog;
        assertThat(savedLog.requestId()).isEqualTo(requestId);
        assertThat(savedLog.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(savedLog.requestedAt()).isEqualTo(REQUESTED_AT);
        assertThat(savedLog.sentAt()).isEqualTo(SENT_AT);
    }

    @Test
    void returnsNotificationLogByRequestId() {
        UUID requestId = UUID.randomUUID();
        InMemoryNotificationLogRepository notificationLogRepository = new InMemoryNotificationLogRepository();
        notificationLogRepository.savedLog = new NotificationLog(
                requestId,
                "PL123456789",
                "recipient@example.com",
                "PL",
                "DE",
                42,
                "Shipment PL123456789 status update",
                "body",
                NotificationStatus.SENT,
                REQUESTED_AT,
                SENT_AT);
        NotificationFacade facade = new NotificationFacade(
                new EmailMessageFactory(),
                new CapturingEmailSender(),
                notificationLogRepository,
                Clock.fixed(SENT_AT, ZoneOffset.UTC));

        Optional<NotificationLogDto> log = facade.findByRequestId(requestId);

        assertThat(log).isPresent();
        assertThat(log.orElseThrow().requestId()).isEqualTo(requestId);
        assertThat(log.orElseThrow().status()).isEqualTo("SENT");
    }

    private static class CapturingEmailSender implements EmailSender {

        private EmailMessage sentMessage;

        @Override
        public void send(EmailMessage message) {
            this.sentMessage = message;
        }
    }

    private static class InMemoryNotificationLogRepository implements NotificationLogRepository {

        private NotificationLog savedLog;

        @Override
        public void save(NotificationLog log) {
            this.savedLog = log;
        }

        @Override
        public Optional<NotificationLog> findByRequestId(UUID requestId) {
            return Optional.ofNullable(savedLog).filter(log -> log.requestId().equals(requestId));
        }
    }
}

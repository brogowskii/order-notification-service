package io.github.brogowski.order.notification.service.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import io.github.brogowski.order.notification.service.notification.dto.NotificationLogDto;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotificationFacadeTest {

    private static final Instant REQUESTED_AT = Instant.parse("2026-05-21T10:00:00Z");
    private static final Instant SENT_AT = Instant.parse("2026-05-21T10:00:01Z");

    @Test
    void sendsMockEmailAndStoresNotificationLog() {
        CapturingEmailSender emailSender = new CapturingEmailSender();
        JdbcNotificationLogRepository notificationLogRepository = mock(JdbcNotificationLogRepository.class);
        UUID requestId = UUID.randomUUID();
        when(notificationLogRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
        NotificationFacade facade =
                new NotificationFacade(emailSender, notificationLogRepository, Clock.fixed(SENT_AT, ZoneOffset.UTC));

        facade.notify(new NotificationRequestedMessage(
                requestId, "PL123456789", "recipient@example.com", "PL", "DE", 42, REQUESTED_AT));

        EmailMessage sentMessage = emailSender.sentMessages.get(0);
        assertThat(sentMessage.recipientEmail()).isEqualTo("recipient@example.com");
        assertThat(sentMessage.subject()).isEqualTo("Shipment PL123456789 status update");
        assertThat(sentMessage.body()).contains("Shipment number: PL123456789");
        assertThat(sentMessage.body()).contains("Status code: 42");

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(logCaptor.capture());
        NotificationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.requestId()).isEqualTo(requestId);
        assertThat(savedLog.status()).isEqualTo("SENT");
        assertThat(savedLog.requestedAt()).isEqualTo(REQUESTED_AT);
        assertThat(savedLog.sentAt()).isEqualTo(SENT_AT);
    }

    @Test
    void returnsNotificationLogByRequestId() {
        UUID requestId = UUID.randomUUID();
        JdbcNotificationLogRepository notificationLogRepository = mock(JdbcNotificationLogRepository.class);
        NotificationLog notificationLog = new NotificationLog(
                requestId,
                "PL123456789",
                "recipient@example.com",
                "PL",
                "DE",
                42,
                "Shipment PL123456789 status update",
                "body",
                "SENT",
                REQUESTED_AT,
                SENT_AT);
        when(notificationLogRepository.findByRequestId(requestId)).thenReturn(Optional.of(notificationLog));
        NotificationFacade facade = new NotificationFacade(
                new CapturingEmailSender(), notificationLogRepository, Clock.fixed(SENT_AT, ZoneOffset.UTC));

        Optional<NotificationLogDto> log = facade.findByRequestId(requestId);

        assertThat(log).isPresent();
        assertThat(log.orElseThrow().requestId()).isEqualTo(requestId);
        assertThat(log.orElseThrow().status()).isEqualTo("SENT");
    }

    @Test
    void skipsEmailWhenNotificationWasAlreadyLogged() {
        CapturingEmailSender emailSender = new CapturingEmailSender();
        JdbcNotificationLogRepository notificationLogRepository = mock(JdbcNotificationLogRepository.class);
        UUID requestId = UUID.randomUUID();
        NotificationLog notificationLog = new NotificationLog(
                requestId,
                "PL123456789",
                "recipient@example.com",
                "PL",
                "DE",
                42,
                "Shipment PL123456789 status update",
                "body",
                "SENT",
                REQUESTED_AT,
                SENT_AT);
        when(notificationLogRepository.findByRequestId(requestId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(notificationLog));
        NotificationFacade facade =
                new NotificationFacade(emailSender, notificationLogRepository, Clock.fixed(SENT_AT, ZoneOffset.UTC));
        NotificationRequestedMessage message = new NotificationRequestedMessage(
                requestId, "PL123456789", "recipient@example.com", "PL", "DE", 42, REQUESTED_AT);

        facade.notify(message);
        facade.notify(message);

        assertThat(emailSender.sentMessages).hasSize(1);
        verify(notificationLogRepository, times(1)).save(org.mockito.ArgumentMatchers.any(NotificationLog.class));
    }

    @Test
    void rejectsInvalidConsumerMessageBeforeSendingEmail() {
        CapturingEmailSender emailSender = new CapturingEmailSender();
        JdbcNotificationLogRepository notificationLogRepository = mock(JdbcNotificationLogRepository.class);
        NotificationFacade facade =
                new NotificationFacade(emailSender, notificationLogRepository, Clock.fixed(SENT_AT, ZoneOffset.UTC));

        assertThatThrownBy(() -> facade.notify(new NotificationRequestedMessage(
                        UUID.randomUUID(), "A".repeat(101), "recipient@example.com", "PL", "DE", 42, REQUESTED_AT)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shipment number must not be longer than 100 characters");

        assertThat(emailSender.sentMessages).isEmpty();
        verifyNoInteractions(notificationLogRepository);
    }

    private static class CapturingEmailSender implements EmailSender {

        private final List<EmailMessage> sentMessages = new ArrayList<>();

        @Override
        public void send(EmailMessage message) {
            sentMessages.add(message);
        }
    }
}

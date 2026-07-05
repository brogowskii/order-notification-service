package io.github.brogowski.order.notification.service.notification.domain;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import io.github.brogowski.order.notification.service.notification.dto.NotificationLogDto;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class NotificationFacade {

    private final EmailSender emailSender;
    private final JdbcNotificationLogRepository notificationLogRepository;
    private final Clock clock;

    NotificationFacade(EmailSender emailSender, JdbcNotificationLogRepository notificationLogRepository, Clock clock) {
        this.emailSender = emailSender;
        this.notificationLogRepository = notificationLogRepository;
        this.clock = clock;
    }

    public void notify(NotificationRequestedMessage message) {
        NotificationRequest request = NotificationRequest.from(message);
        if (notificationLogRepository.findByRequestId(request.requestId()).isPresent()) {
            return;
        }

        EmailMessage emailMessage = createEmailMessage(request);
        emailSender.send(emailMessage);
        notificationLogRepository.save(NotificationLog.sent(request, emailMessage, Instant.now(clock)));
    }

    public Optional<NotificationLogDto> findByRequestId(UUID requestId) {
        return notificationLogRepository.findByRequestId(requestId).map(NotificationLog::toDto);
    }

    private static EmailMessage createEmailMessage(NotificationRequest request) {
        return new EmailMessage(
                request.recipientEmail(), "Shipment " + request.shipmentNumber() + " status update", """
        Shipment number: %s
        Recipient email: %s
        Recipient country: %s
        Sender country: %s
        Status code: %d
        """.formatted(
                                request.shipmentNumber(),
                                request.recipientEmail(),
                                request.recipientCountryCode(),
                                request.senderCountryCode(),
                                request.statusCode()));
    }
}

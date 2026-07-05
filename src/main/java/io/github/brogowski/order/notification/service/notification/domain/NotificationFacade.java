package io.github.brogowski.order.notification.service.notification.domain;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import io.github.brogowski.order.notification.service.notification.dto.NotificationLogDto;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class NotificationFacade {

    private final EmailMessageFactory emailMessageFactory;
    private final EmailSender emailSender;
    private final NotificationLogRepository notificationLogRepository;
    private final Clock clock;

    NotificationFacade(
            EmailMessageFactory emailMessageFactory,
            EmailSender emailSender,
            NotificationLogRepository notificationLogRepository,
            Clock clock) {
        this.emailMessageFactory = emailMessageFactory;
        this.emailSender = emailSender;
        this.notificationLogRepository = notificationLogRepository;
        this.clock = clock;
    }

    public void notify(NotificationRequestedMessage message) {
        NotificationRequest request = NotificationRequest.from(message);
        if (notificationLogRepository.findByRequestId(request.requestId()).isPresent()) {
            return;
        }

        EmailMessage emailMessage = emailMessageFactory.create(request);
        emailSender.send(emailMessage);
        notificationLogRepository.save(NotificationLog.sent(request, emailMessage, Instant.now(clock)));
    }

    public Optional<NotificationLogDto> findByRequestId(UUID requestId) {
        return notificationLogRepository.findByRequestId(requestId).map(NotificationLog::toDto);
    }
}

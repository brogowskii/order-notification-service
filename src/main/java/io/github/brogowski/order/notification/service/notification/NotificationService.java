package io.github.brogowski.order.notification.service.notification;

import io.github.brogowski.order.notification.service.messaging.NotificationRequestedMessage;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class NotificationService implements NotificationFacade {

  private final EmailMessageFactory emailMessageFactory;
  private final EmailSender emailSender;
  private final NotificationLogRepository notificationLogRepository;
  private final Clock clock;

  NotificationService(
      EmailMessageFactory emailMessageFactory,
      EmailSender emailSender,
      NotificationLogRepository notificationLogRepository,
      Clock clock) {
    this.emailMessageFactory = emailMessageFactory;
    this.emailSender = emailSender;
    this.notificationLogRepository = notificationLogRepository;
    this.clock = clock;
  }

  void notify(NotificationRequestedMessage message) {
    EmailMessage emailMessage = emailMessageFactory.create(message);
    emailSender.send(emailMessage);
    notificationLogRepository.save(NotificationLog.sent(message, emailMessage, Instant.now(clock)));
  }

  @Override
  public Optional<NotificationLogDto> findByRequestId(UUID requestId) {
    return notificationLogRepository.findByRequestId(requestId).map(NotificationLog::toDto);
  }
}

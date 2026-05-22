package io.github.brogowski.order.notification.service.notificationoutbox;

import org.springframework.stereotype.Service;

@Service
class NotificationOutboxService implements NotificationOutboxFacade {

  private final NotificationOutboxRepository notificationOutboxRepository;

  NotificationOutboxService(NotificationOutboxRepository notificationOutboxRepository) {
    this.notificationOutboxRepository = notificationOutboxRepository;
  }

  @Override
  public void schedule(NotificationOutboxCommand command) {
    notificationOutboxRepository.save(NotificationOutboxEntry.from(command));
  }
}

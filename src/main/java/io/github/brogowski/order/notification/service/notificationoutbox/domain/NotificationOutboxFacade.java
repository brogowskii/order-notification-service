package io.github.brogowski.order.notification.service.notificationoutbox.domain;

public class NotificationOutboxFacade {

  private final NotificationOutboxRepository notificationOutboxRepository;

  NotificationOutboxFacade(NotificationOutboxRepository notificationOutboxRepository) {
    this.notificationOutboxRepository = notificationOutboxRepository;
  }

  public void schedule(NotificationOutboxCommand command) {
    notificationOutboxRepository.save(NotificationOutboxEntry.from(command));
  }
}

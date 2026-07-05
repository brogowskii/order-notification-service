package io.github.brogowski.order.notification.service.notificationoutbox.domain;

public class NotificationOutboxFacade {

    private final JdbcNotificationOutboxRepository notificationOutboxRepository;

    NotificationOutboxFacade(JdbcNotificationOutboxRepository notificationOutboxRepository) {
        this.notificationOutboxRepository = notificationOutboxRepository;
    }

    public void schedule(NotificationOutboxCommand command) {
        notificationOutboxRepository.save(NotificationOutboxEntry.from(command));
    }
}

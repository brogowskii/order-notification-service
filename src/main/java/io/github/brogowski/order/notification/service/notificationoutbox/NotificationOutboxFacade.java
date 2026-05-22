package io.github.brogowski.order.notification.service.notificationoutbox;

public interface NotificationOutboxFacade {

  void schedule(NotificationOutboxCommand command);
}

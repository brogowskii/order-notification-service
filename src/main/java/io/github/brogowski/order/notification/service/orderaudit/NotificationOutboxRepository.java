package io.github.brogowski.order.notification.service.orderaudit;

interface NotificationOutboxRepository {

  void save(NotificationOutboxEntry entry);
}

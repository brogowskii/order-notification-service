package io.github.brogowski.order.notification.service.notificationoutbox;

enum OutboxStatus {
  PENDING,
  PROCESSING,
  PUBLISHED,
  FAILED
}

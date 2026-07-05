package io.github.brogowski.order.notification.service.notificationoutbox.domain;

enum OutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}

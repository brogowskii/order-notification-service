package io.github.brogowski.order.notification.service.orderaudit;

class NotificationOutboxPublishingException extends RuntimeException {

  NotificationOutboxPublishingException(String message, Throwable cause) {
    super(message, cause);
  }
}

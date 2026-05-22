package io.github.brogowski.order.notification.service.notificationoutbox;

class NotificationOutboxPublishingException extends RuntimeException {

  NotificationOutboxPublishingException(String message, Throwable cause) {
    super(message, cause);
  }
}

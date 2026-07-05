package io.github.brogowski.order.notification.service.notificationoutbox.exception;

public class NotificationOutboxPublishingException extends RuntimeException {

    public NotificationOutboxPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}

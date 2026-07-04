package io.github.brogowski.order.notification.service.orderintake.exception;

public class OrderIntakeRateLimitExceededException extends RuntimeException {

  public OrderIntakeRateLimitExceededException(String message) {
    super(message);
  }
}

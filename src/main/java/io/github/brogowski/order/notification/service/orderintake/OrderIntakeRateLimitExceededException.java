package io.github.brogowski.order.notification.service.orderintake;

class OrderIntakeRateLimitExceededException extends RuntimeException {

  OrderIntakeRateLimitExceededException(String message) {
    super(message);
  }
}

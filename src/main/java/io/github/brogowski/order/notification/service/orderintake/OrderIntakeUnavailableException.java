package io.github.brogowski.order.notification.service.orderintake;

class OrderIntakeUnavailableException extends RuntimeException {

  OrderIntakeUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}

package io.github.brogowski.order.notification.service.orderintake.domain;

record StatusCode(int value) {

  StatusCode {
    if (value < 0 || value > 100) {
      throw new IllegalArgumentException("Status code must be between 0 and 100");
    }
  }
}

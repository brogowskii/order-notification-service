package io.github.brogowski.order.notification.service.orderintake.domain;

class NoopOrderIntakeRateLimiter implements OrderIntakeRateLimiter {

  @Override
  public boolean tryAcquire() {
    return true;
  }
}

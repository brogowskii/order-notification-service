package io.github.brogowski.order.notification.service.orderintake;

interface OrderIntakeRateLimiter {

  boolean tryAcquire();
}

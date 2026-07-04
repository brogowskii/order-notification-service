package io.github.brogowski.order.notification.service.orderintake.domain;

interface OrderIntakeRateLimiter {

  boolean tryAcquire();
}

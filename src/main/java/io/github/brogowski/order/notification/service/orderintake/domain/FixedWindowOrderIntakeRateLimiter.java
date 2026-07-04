package io.github.brogowski.order.notification.service.orderintake.domain;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

class FixedWindowOrderIntakeRateLimiter implements OrderIntakeRateLimiter {

  private final Clock clock;
  private final int maxRequests;
  private final Duration window;
  private Instant windowStartedAt;
  private int usedRequests;

  FixedWindowOrderIntakeRateLimiter(Clock clock, int maxRequests, Duration window) {
    if (maxRequests < 1) {
      throw new IllegalArgumentException("Order intake max requests must be positive");
    }
    if (window.isZero() || window.isNegative()) {
      throw new IllegalArgumentException("Order intake rate limit window must be positive");
    }
    this.clock = clock;
    this.maxRequests = maxRequests;
    this.window = window;
    this.windowStartedAt = Instant.now(clock);
  }

  @Override
  public synchronized boolean tryAcquire() {
    Instant now = Instant.now(clock);
    if (!now.isBefore(windowStartedAt.plus(window))) {
      windowStartedAt = now;
      usedRequests = 0;
    }
    if (usedRequests >= maxRequests) {
      return false;
    }
    usedRequests++;
    return true;
  }
}

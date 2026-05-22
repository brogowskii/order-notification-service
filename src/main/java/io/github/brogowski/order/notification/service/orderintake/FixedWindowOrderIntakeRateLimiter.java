package io.github.brogowski.order.notification.service.orderintake;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "app.intake.rate-limit.enabled",
    havingValue = "true",
    matchIfMissing = true)
class FixedWindowOrderIntakeRateLimiter implements OrderIntakeRateLimiter {

  private final Clock clock;
  private final int maxRequests;
  private final Duration window;
  private Instant windowStartedAt;
  private int usedRequests;

  FixedWindowOrderIntakeRateLimiter(
      Clock clock,
      @Value("${app.intake.rate-limit.max-requests}") int maxRequests,
      @Value("${app.intake.rate-limit.window}") Duration window) {
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

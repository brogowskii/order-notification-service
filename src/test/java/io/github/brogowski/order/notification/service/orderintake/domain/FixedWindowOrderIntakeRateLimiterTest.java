package io.github.brogowski.order.notification.service.orderintake.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class FixedWindowOrderIntakeRateLimiterTest {

  @Test
  void rejectsRequestsAfterLimitIsReached() {
    MutableClock clock = new MutableClock(Instant.parse("2026-05-22T10:00:00Z"));
    FixedWindowOrderIntakeRateLimiter rateLimiter =
        new FixedWindowOrderIntakeRateLimiter(clock, 2, Duration.ofSeconds(1));

    assertThat(rateLimiter.tryAcquire()).isTrue();
    assertThat(rateLimiter.tryAcquire()).isTrue();
    assertThat(rateLimiter.tryAcquire()).isFalse();
  }

  @Test
  void allowsRequestsAfterWindowResets() {
    MutableClock clock = new MutableClock(Instant.parse("2026-05-22T10:00:00Z"));
    FixedWindowOrderIntakeRateLimiter rateLimiter =
        new FixedWindowOrderIntakeRateLimiter(clock, 1, Duration.ofSeconds(1));

    assertThat(rateLimiter.tryAcquire()).isTrue();
    assertThat(rateLimiter.tryAcquire()).isFalse();

    clock.advanceBy(Duration.ofSeconds(1));

    assertThat(rateLimiter.tryAcquire()).isTrue();
  }

  private static class MutableClock extends Clock {

    private Instant now;

    private MutableClock(Instant now) {
      this.now = now;
    }

    void advanceBy(Duration duration) {
      now = now.plus(duration);
    }

    @Override
    public ZoneId getZone() {
      return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }
}

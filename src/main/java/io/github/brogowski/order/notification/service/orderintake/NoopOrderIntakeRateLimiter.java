package io.github.brogowski.order.notification.service.orderintake;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.intake.rate-limit.enabled", havingValue = "false")
class NoopOrderIntakeRateLimiter implements OrderIntakeRateLimiter {

  @Override
  public boolean tryAcquire() {
    return true;
  }
}

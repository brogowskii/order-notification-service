package io.github.brogowski.order.notification.service.orderintake.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
class OrderIntakeModuleConfiguration {

  @Bean
  OrderIntakeFacade orderIntakeFacade(
      KafkaTemplate<String, OrderReceivedMessage> kafkaTemplate,
      Clock clock,
      @Value("${app.kafka.topics.orders-received}") String topicName,
      @Value("${app.intake.rate-limit.enabled:false}") boolean rateLimitEnabled,
      @Value("${app.intake.rate-limit.max-requests}") int maxRequests,
      @Value("${app.intake.rate-limit.window}") Duration window) {
    return new OrderIntakeFacade(
        new KafkaOrderReceivedPublisher(kafkaTemplate, topicName),
        rateLimiter(rateLimitEnabled, clock, maxRequests, window),
        clock);
  }

  private OrderIntakeRateLimiter rateLimiter(
      boolean rateLimitEnabled, Clock clock, int maxRequests, Duration window) {
    if (!rateLimitEnabled) {
      return new NoopOrderIntakeRateLimiter();
    }
    return new FixedWindowOrderIntakeRateLimiter(clock, maxRequests, window);
  }
}

package io.github.brogowski.order.notification.service.orderintake.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import java.time.Clock;
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
      @Value("${app.kafka.topics.orders-received}") String topicName) {
    final KafkaOrderReceivedPublisher orderReceivedPublisher = new KafkaOrderReceivedPublisher(
        kafkaTemplate, topicName);

    return new OrderIntakeFacade(orderReceivedPublisher, clock);
  }
}

package io.github.brogowski.order.notification.service.orderintake;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaOrderReceivedPublisher implements OrderReceivedPublisher {

  private final KafkaTemplate<String, OrderReceivedMessage> kafkaTemplate;
  private final String topicName;
  private final Duration publishTimeout;

  KafkaOrderReceivedPublisher(
      KafkaTemplate<String, OrderReceivedMessage> kafkaTemplate,
      @Value("${app.kafka.topics.orders-received}") String topicName,
      @Value("${app.kafka.publish-timeout}") Duration publishTimeout) {
    this.kafkaTemplate = kafkaTemplate;
    this.topicName = topicName;
    this.publishTimeout = publishTimeout;
  }

  @Override
  public void publish(OrderReceivedMessage message) {
    try {
      kafkaTemplate
          .send(topicName, message.shipmentNumber(), message)
          .get(publishTimeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new OrderIntakeUnavailableException("Publishing order request was interrupted", exception);
    } catch (ExecutionException | TimeoutException exception) {
      throw new OrderIntakeUnavailableException("Could not publish order request", exception);
    }
  }
}

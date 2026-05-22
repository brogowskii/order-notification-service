package io.github.brogowski.order.notification.service.orderaudit;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class OrderReceivedKafkaListener {

  private final OrderAuditService orderAuditService;

  OrderReceivedKafkaListener(OrderAuditService orderAuditService) {
    this.orderAuditService = orderAuditService;
  }

  @KafkaListener(
      topics = "${app.kafka.topics.orders-received}",
      groupId = "${app.kafka.consumers.order-audit.group-id}",
      containerFactory = "orderReceivedKafkaListenerContainerFactory")
  void onOrderReceived(OrderReceivedMessage message) {
    orderAuditService.audit(message);
  }
}

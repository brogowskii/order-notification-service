package io.github.brogowski.order.notification.service.orderaudit.messaging;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;
import io.github.brogowski.order.notification.service.orderaudit.domain.OrderAuditFacade;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class OrderReceivedKafkaListener {

  private final OrderAuditFacade orderAuditFacade;

  OrderReceivedKafkaListener(OrderAuditFacade orderAuditFacade) {
    this.orderAuditFacade = orderAuditFacade;
  }

  @KafkaListener(
      topics = "${app.kafka.topics.orders-received}",
      groupId = "${app.kafka.consumers.order-audit.group-id}",
      containerFactory = "orderReceivedKafkaListenerContainerFactory")
  void onOrderReceived(OrderReceivedMessage message) {
    orderAuditFacade.audit(message);
  }
}

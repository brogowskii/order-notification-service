package io.github.brogowski.order.notification.service.orderintake.domain;

import io.github.brogowski.order.notification.service.messaging.OrderReceivedMessage;

interface OrderReceivedPublisher {

  void publish(OrderReceivedMessage message);
}

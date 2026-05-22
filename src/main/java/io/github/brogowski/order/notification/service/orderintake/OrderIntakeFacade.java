package io.github.brogowski.order.notification.service.orderintake;

public interface OrderIntakeFacade {

  OrderAcceptedDto accept(OrderRequestDto request);
}

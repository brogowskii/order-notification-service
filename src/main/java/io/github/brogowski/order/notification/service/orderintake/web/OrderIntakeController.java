package io.github.brogowski.order.notification.service.orderintake.web;

import io.github.brogowski.order.notification.service.orderintake.domain.OrderIntakeFacade;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderAcceptedDto;
import io.github.brogowski.order.notification.service.orderintake.dto.OrderRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
class OrderIntakeController {

  private final OrderIntakeFacade orderIntakeFacade;

  OrderIntakeController(OrderIntakeFacade orderIntakeFacade) {
    this.orderIntakeFacade = orderIntakeFacade;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  OrderAcceptedDto accept(@Valid @RequestBody OrderRequestDto request) {
    return orderIntakeFacade.accept(request);
  }
}

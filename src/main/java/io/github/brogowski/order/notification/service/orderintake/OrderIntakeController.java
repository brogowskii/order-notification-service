package io.github.brogowski.order.notification.service.orderintake;

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
  private final OrderIntakeRateLimiter orderIntakeRateLimiter;

  OrderIntakeController(
      OrderIntakeFacade orderIntakeFacade, OrderIntakeRateLimiter orderIntakeRateLimiter) {
    this.orderIntakeFacade = orderIntakeFacade;
    this.orderIntakeRateLimiter = orderIntakeRateLimiter;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.ACCEPTED)
  OrderAcceptedDto accept(@Valid @RequestBody OrderRequestDto request) {
    if (!orderIntakeRateLimiter.tryAcquire()) {
      throw new OrderIntakeRateLimitExceededException("Order intake rate limit exceeded");
    }
    return orderIntakeFacade.accept(request);
  }
}

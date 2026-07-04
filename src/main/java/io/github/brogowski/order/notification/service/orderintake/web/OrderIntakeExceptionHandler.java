package io.github.brogowski.order.notification.service.orderintake.web;

import io.github.brogowski.order.notification.service.orderintake.exception.OrderIntakeUnavailableException;
import io.github.brogowski.order.notification.service.shared.ApiErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = OrderIntakeController.class)
class OrderIntakeExceptionHandler {

  private final Clock clock;

  OrderIntakeExceptionHandler(Clock clock) {
    this.clock = clock;
  }

  @ExceptionHandler(OrderIntakeUnavailableException.class)
  ResponseEntity<ApiErrorDto> handleUnavailable(
      OrderIntakeUnavailableException exception, HttpServletRequest request) {
    HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
    return ResponseEntity.status(status)
        .body(
            new ApiErrorDto(
                Instant.now(clock),
                status.value(),
                status.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI()));
  }
}

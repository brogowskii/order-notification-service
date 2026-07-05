package io.github.brogowski.order.notification.service.orderintake.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class OrderRequestDtoTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsShipmentNumberLongerThanDatabaseColumn() {
        OrderRequestDto request = new OrderRequestDto("A".repeat(101), "recipient@example.com", "PL", "DE", 42);

        assertThat(validator.validate(request))
                .anySatisfy(violation ->
                        assertThat(violation.getPropertyPath().toString()).isEqualTo("shipmentNumber"));
    }

    @Test
    void rejectsRecipientEmailLongerThanDatabaseColumn() {
        OrderRequestDto request = new OrderRequestDto("PL123456789", "a".repeat(310) + "@example.com", "PL", "DE", 42);

        assertThat(validator.validate(request))
                .anySatisfy(violation ->
                        assertThat(violation.getPropertyPath().toString()).isEqualTo("recipientEmail"));
    }
}

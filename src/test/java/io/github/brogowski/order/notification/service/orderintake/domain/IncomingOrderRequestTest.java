package io.github.brogowski.order.notification.service.orderintake.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.brogowski.order.notification.service.orderintake.dto.OrderRequestDto;
import org.junit.jupiter.api.Test;

class IncomingOrderRequestTest {

    @Test
    void createsDomainModelFromDto() {
        IncomingOrderRequest request = IncomingOrderRequest.from(
                new OrderRequestDto(" PL123456789 ", "recipient@example.com", "PL", "DE", 42));

        assertThat(request.shipmentNumber().value()).isEqualTo("PL123456789");
        assertThat(request.recipientEmail().value()).isEqualTo("recipient@example.com");
        assertThat(request.recipientCountryCode().value()).isEqualTo("PL");
        assertThat(request.senderCountryCode().value()).isEqualTo("DE");
        assertThat(request.statusCode().value()).isEqualTo(42);
    }

    @Test
    void rejectsInvalidStatusCode() {
        assertThatThrownBy(() -> new StatusCode(101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Status code must be between 0 and 100");
    }

    @Test
    void rejectsInvalidCountryCode() {
        assertThatThrownBy(() -> new CountryCode("pol"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Country code must use ISO alpha-2 uppercase format");
    }

    @Test
    void rejectsShipmentNumberLongerThanDatabaseColumn() {
        assertThatThrownBy(() -> new ShipmentNumber("A".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Shipment number must not be longer than 100 characters");
    }

    @Test
    void rejectsRecipientEmailLongerThanDatabaseColumn() {
        assertThatThrownBy(() -> new RecipientEmail("a".repeat(310) + "@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Recipient email must not be longer than 320 characters");
    }
}

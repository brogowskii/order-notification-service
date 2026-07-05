package io.github.brogowski.order.notification.service.orderintake.domain;

import io.github.brogowski.order.notification.service.orderintake.dto.OrderRequestDto;

record IncomingOrderRequest(
        ShipmentNumber shipmentNumber,
        RecipientEmail recipientEmail,
        CountryCode recipientCountryCode,
        CountryCode senderCountryCode,
        StatusCode statusCode) {

    static IncomingOrderRequest from(OrderRequestDto request) {
        return new IncomingOrderRequest(
                new ShipmentNumber(request.shipmentNumber()),
                new RecipientEmail(request.recipientEmail()),
                new CountryCode(request.recipientCountryCode()),
                new CountryCode(request.senderCountryCode()),
                new StatusCode(request.statusCode()));
    }
}

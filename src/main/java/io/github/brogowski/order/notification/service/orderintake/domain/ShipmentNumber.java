package io.github.brogowski.order.notification.service.orderintake.domain;

import org.springframework.util.StringUtils;

record ShipmentNumber(String value) {

    private static final int MAX_LENGTH = 100;

    ShipmentNumber {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Shipment number must not be blank");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Shipment number must not be longer than 100 characters");
        }
    }
}

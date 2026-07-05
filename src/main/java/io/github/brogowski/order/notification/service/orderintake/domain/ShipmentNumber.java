package io.github.brogowski.order.notification.service.orderintake.domain;

import org.springframework.util.StringUtils;

record ShipmentNumber(String value) {

    ShipmentNumber {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Shipment number must not be blank");
        }
        value = value.trim();
    }
}

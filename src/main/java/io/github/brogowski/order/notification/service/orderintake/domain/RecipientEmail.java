package io.github.brogowski.order.notification.service.orderintake.domain;

import org.springframework.util.StringUtils;

record RecipientEmail(String value) {

    RecipientEmail {
        if (!StringUtils.hasText(value) || !value.contains("@")) {
            throw new IllegalArgumentException("Recipient email must be valid");
        }
        value = value.trim();
    }
}

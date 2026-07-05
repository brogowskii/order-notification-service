package io.github.brogowski.order.notification.service.orderintake.domain;

import org.springframework.util.StringUtils;

record RecipientEmail(String value) {

    private static final int MAX_LENGTH = 320;

    RecipientEmail {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Recipient email must be valid");
        }
        value = value.trim();
        if (!value.contains("@")) {
            throw new IllegalArgumentException("Recipient email must be valid");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Recipient email must not be longer than 320 characters");
        }
    }
}

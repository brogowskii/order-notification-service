package io.github.brogowski.order.notification.service.shared;

import java.time.Instant;

public record ApiErrorDto(
    Instant timestamp, int status, String error, String message, String path) {}

package io.github.brogowski.order.notification.service.orderintake.dto;

import java.time.Instant;
import java.util.UUID;

public record OrderAcceptedDto(UUID requestId, Instant acceptedAt) {}

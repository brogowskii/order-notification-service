package io.github.brogowski.order.notification.service.orderintake;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OrderRequestDto(
    @NotBlank String shipmentNumber,
    @NotBlank @Email String recipientEmail,
    @NotBlank @Pattern(regexp = "^[A-Z]{2}$") String recipientCountryCode,
    @NotBlank @Pattern(regexp = "^[A-Z]{2}$") String senderCountryCode,
    @NotNull @Min(0) @Max(100) Integer statusCode) {}

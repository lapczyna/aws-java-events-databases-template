package com.enterprise.adplatform.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class AdImpressionRequest {

    @NotBlank(message = "Campaign ID is required")
    private String campaignId;

    @NotBlank(message = "Placement ID is required")
    private String placementId;

    @NotNull(message = "Timestamp is required")
    private Instant timestamp;

    @NotBlank(message = "Device type is required")
    @Size(max = 50)
    private String deviceType;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 3, message = "Country must be ISO 3166-1 alpha-2 or alpha-3 code")
    private String country;

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 6)
    private BigDecimal cost;
}

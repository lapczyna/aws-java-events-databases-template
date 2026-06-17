package com.enterprise.adplatform.dto;

import com.enterprise.adplatform.entity.Campaign;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CampaignRequest {

    @NotNull(message = "Advertiser ID is required")
    @Positive(message = "Advertiser ID must be positive")
    private Long advertiserId;

    @NotBlank(message = "Campaign name is required")
    @Size(min = 2, max = 255, message = "Campaign name must be between 2 and 255 characters")
    private String campaignName;

    @NotNull(message = "Campaign type is required")
    private Campaign.CampaignType campaignType;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.01", message = "Budget must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Budget format invalid")
    private BigDecimal budget;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "Status is required")
    private Campaign.CampaignStatus status;
}

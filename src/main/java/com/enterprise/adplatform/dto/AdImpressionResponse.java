package com.enterprise.adplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class AdImpressionResponse {

    private String impressionId;
    private String campaignId;
    private String placementId;
    private Instant timestamp;
    private String deviceType;
    private String country;
    private String eventType;
    private BigDecimal cost;
}

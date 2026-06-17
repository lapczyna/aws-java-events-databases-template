package com.enterprise.adplatform.dto;

import com.enterprise.adplatform.entity.Campaign;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CampaignResponse {

    private Long id;
    private Long advertiserId;
    private String campaignName;
    private Campaign.CampaignType campaignType;
    private BigDecimal budget;
    private LocalDate startDate;
    private LocalDate endDate;
    private Campaign.CampaignStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

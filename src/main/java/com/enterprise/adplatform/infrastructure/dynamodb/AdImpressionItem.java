package com.enterprise.adplatform.infrastructure.dynamodb;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;

@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdImpressionItem {

    private String impressionId;
    private String campaignId;
    private String placementId;
    private Instant timestamp;
    private String deviceType;
    private String country;
    private String eventType;
    private BigDecimal cost;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("impressionId")
    public String getImpressionId() {
        return impressionId;
    }

    @DynamoDbAttribute("campaignId")
    public String getCampaignId() {
        return campaignId;
    }

    @DynamoDbAttribute("placementId")
    public String getPlacementId() {
        return placementId;
    }

    @DynamoDbAttribute("timestamp")
    public Instant getTimestamp() {
        return timestamp;
    }

    @DynamoDbAttribute("deviceType")
    public String getDeviceType() {
        return deviceType;
    }

    @DynamoDbAttribute("country")
    public String getCountry() {
        return country;
    }

    @DynamoDbAttribute("eventType")
    public String getEventType() {
        return eventType;
    }

    @DynamoDbAttribute("cost")
    public BigDecimal getCost() {
        return cost;
    }
}

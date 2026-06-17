package com.enterprise.adplatform.messaging.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEvent {

    private String eventId;
    private String eventType;
    private String campaignId;
    private String campaignName;
    private String advertiserId;
    private Instant occurredAt;
}

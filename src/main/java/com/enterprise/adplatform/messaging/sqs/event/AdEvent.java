package com.enterprise.adplatform.messaging.sqs.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CampaignCreatedEvent.class, name = "CAMPAIGN_CREATED"),
    @JsonSubTypes.Type(value = CampaignPausedEvent.class,  name = "CAMPAIGN_PAUSED"),
    @JsonSubTypes.Type(value = AdImpressionReceivedEvent.class, name = "AD_IMPRESSION_RECEIVED")
})
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AdEvent {

    private String eventId;
    private String eventType;
    private Instant occurredAt;
}

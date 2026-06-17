package com.enterprise.adplatform.messaging.sqs.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class CampaignPausedEvent extends AdEvent {

    private String campaignId;
    private String campaignName;
}

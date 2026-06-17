package com.enterprise.adplatform.messaging.sqs.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class AdImpressionReceivedEvent extends AdEvent {

    private String impressionId;
    private String campaignId;
    private String placementId;
    private BigDecimal cost;
}

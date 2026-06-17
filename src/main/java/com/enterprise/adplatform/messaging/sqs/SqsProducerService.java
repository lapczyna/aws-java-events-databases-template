package com.enterprise.adplatform.messaging.sqs;

import com.enterprise.adplatform.messaging.sqs.event.AdImpressionReceivedEvent;
import com.enterprise.adplatform.messaging.sqs.event.CampaignCreatedEvent;
import com.enterprise.adplatform.messaging.sqs.event.CampaignPausedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsProducerService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    public void sendCampaignCreated(CampaignCreatedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("CAMPAIGN_CREATED");
        send(event);
    }

    public void sendCampaignPaused(CampaignPausedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("CAMPAIGN_PAUSED");
        send(event);
    }

    public void sendAdImpressionReceived(AdImpressionReceivedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("AD_IMPRESSION_RECEIVED");
        send(event);
    }

    private void send(Object event) {
        try {
            String body = objectMapper.writeValueAsString(event);
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .messageGroupId("ad-events")
                    .messageDeduplicationId(UUID.randomUUID().toString())
                    .build();
            var result = sqsClient.sendMessage(request);
            log.info("SQS message sent messageId={} eventType={}", result.messageId(), event.getClass().getSimpleName());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SQS event", e);
            throw new RuntimeException("SQS serialization failed", e);
        }
    }
}

package com.enterprise.adplatform.messaging.sqs;

import com.enterprise.adplatform.messaging.sqs.event.AdEvent;
import com.enterprise.adplatform.messaging.sqs.event.AdImpressionReceivedEvent;
import com.enterprise.adplatform.messaging.sqs.event.CampaignCreatedEvent;
import com.enterprise.adplatform.messaging.sqs.event.CampaignPausedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsConsumerService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    @Scheduled(fixedDelayString = "${aws.sqs.polling-interval-ms:5000}")
    public void pollMessages() {
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();
        for (Message message : messages) {
            processMessage(message);
        }
    }

    private void processMessage(Message message) {
        try {
            log.info("Received SQS message messageId={}", message.messageId());
            AdEvent event = objectMapper.readValue(message.body(), AdEvent.class);
            handleEvent(event);
            deleteMessage(message);
        } catch (Exception e) {
            log.error("Failed to process SQS message messageId={}", message.messageId(), e);
        }
    }

    private void handleEvent(AdEvent event) {
        switch (event) {
            case CampaignCreatedEvent e ->
                log.info("Processing CAMPAIGN_CREATED campaignId={} name={} advertiserId={}",
                        e.getCampaignId(), e.getCampaignName(), e.getAdvertiserId());
            case CampaignPausedEvent e ->
                log.info("Processing CAMPAIGN_PAUSED campaignId={} name={}",
                        e.getCampaignId(), e.getCampaignName());
            case AdImpressionReceivedEvent e ->
                log.info("Processing AD_IMPRESSION_RECEIVED impressionId={} campaignId={} cost={}",
                        e.getImpressionId(), e.getCampaignId(), e.getCost());
            default ->
                log.warn("Unknown event type received eventType={}", event.getEventType());
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsClient.deleteMessage(deleteRequest);
        log.debug("SQS message deleted messageId={}", message.messageId());
    }
}

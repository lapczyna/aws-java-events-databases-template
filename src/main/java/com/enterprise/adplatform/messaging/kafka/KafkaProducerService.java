package com.enterprise.adplatform.messaging.kafka;

import com.enterprise.adplatform.messaging.kafka.event.CampaignEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, CampaignEvent> kafkaTemplate;

    @Value("${kafka.topics.campaign-events:campaign-events}")
    private String campaignEventsTopic;

    public void sendCampaignEvent(CampaignEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        String key = event.getCampaignId();
        CompletableFuture<SendResult<String, CampaignEvent>> future =
                kafkaTemplate.send(campaignEventsTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Kafka event sent topic={} partition={} offset={} eventId={}",
                        campaignEventsTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getEventId());
            } else {
                log.error("Failed to send Kafka event eventId={}", event.getEventId(), ex);
            }
        });
    }
}

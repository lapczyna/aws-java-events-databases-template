package com.enterprise.adplatform.messaging.kafka;

import com.enterprise.adplatform.messaging.kafka.event.CampaignEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

    @KafkaListener(
            topics = "${kafka.topics.campaign-events:campaign-events}",
            groupId = "${spring.kafka.consumer.group-id:ad-platform-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCampaignEvent(ConsumerRecord<String, CampaignEvent> record, Acknowledgment ack) {
        try {
            CampaignEvent event = record.value();
            log.info("Kafka event received topic={} partition={} offset={} eventType={} campaignId={} eventId={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    event.getEventType(),
                    event.getCampaignId(),
                    event.getEventId());

            processEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing Kafka message key={} offset={}", record.key(), record.offset(), e);
        }
    }

    private void processEvent(CampaignEvent event) {
        switch (event.getEventType()) {
            case "CAMPAIGN_CREATED" ->
                log.info("Campaign created via Kafka campaignId={} name={}", event.getCampaignId(), event.getCampaignName());
            case "CAMPAIGN_PAUSED" ->
                log.info("Campaign paused via Kafka campaignId={}", event.getCampaignId());
            case "CAMPAIGN_DELETED" ->
                log.info("Campaign deleted via Kafka campaignId={}", event.getCampaignId());
            default ->
                log.warn("Unknown Kafka event type={}", event.getEventType());
        }
    }
}

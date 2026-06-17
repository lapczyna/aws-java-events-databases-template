package com.enterprise.adplatform.integration;

import com.enterprise.adplatform.messaging.kafka.KafkaProducerService;
import com.enterprise.adplatform.messaging.kafka.event.CampaignEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"campaign-events-test"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9093", "port=9093"}
)
@DirtiesContext
@DisplayName("Kafka Integration Tests")
class KafkaIntegrationTest {

    @Autowired KafkaProducerService kafkaProducerService;
    @Autowired EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    @DisplayName("sends CampaignEvent to Kafka topic and consumer receives it")
    void sendCampaignEvent_isConsumedSuccessfully() throws InterruptedException {
        BlockingQueue<ConsumerRecord<String, CampaignEvent>> records = new LinkedBlockingQueue<>();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<CampaignEvent> deserializer = new JsonDeserializer<>(CampaignEvent.class);
        deserializer.addTrustedPackages("*");

        DefaultKafkaConsumerFactory<String, CampaignEvent> factory = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), deserializer);

        ContainerProperties containerProps = new ContainerProperties("campaign-events-test");
        KafkaMessageListenerContainer<String, CampaignEvent> container =
                new KafkaMessageListenerContainer<>(factory, containerProps);
        container.setupMessageListener((MessageListener<String, CampaignEvent>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        CampaignEvent event = CampaignEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CAMPAIGN_CREATED")
                .campaignId("campaign-123")
                .campaignName("Kafka Test Campaign")
                .occurredAt(Instant.now())
                .build();

        kafkaProducerService.sendCampaignEvent(event);

        ConsumerRecord<String, CampaignEvent> received = records.poll(10, TimeUnit.SECONDS);
        container.stop();

        assertThat(received).isNotNull();
        assertThat(received.value().getCampaignId()).isEqualTo("campaign-123");
        assertThat(received.value().getEventType()).isEqualTo("CAMPAIGN_CREATED");
    }
}

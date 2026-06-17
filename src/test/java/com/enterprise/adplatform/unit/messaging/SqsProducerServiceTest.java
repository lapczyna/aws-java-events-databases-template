package com.enterprise.adplatform.unit.messaging;

import com.enterprise.adplatform.messaging.sqs.SqsProducerService;
import com.enterprise.adplatform.messaging.sqs.event.CampaignCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SqsProducerService unit tests")
class SqsProducerServiceTest {

    @Mock SqsClient sqsClient;

    SqsProducerService sqsProducerService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        sqsProducerService = new SqsProducerService(sqsClient, objectMapper);
        ReflectionTestUtils.setField(sqsProducerService, "queueUrl",
                "http://localhost:4566/000000000000/ad-events-queue");
    }

    @Test
    @DisplayName("sendCampaignCreated sends message to SQS with correct body structure")
    void sendCampaignCreated_sendsMessage() {
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().messageId("msg-001").build());

        CampaignCreatedEvent event = CampaignCreatedEvent.builder()
                .campaignId("42")
                .campaignName("Test")
                .advertiserId("7")
                .occurredAt(Instant.now())
                .build();

        sqsProducerService.sendCampaignCreated(event);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest sent = captor.getValue();
        assertThat(sent.queueUrl()).isEqualTo("http://localhost:4566/000000000000/ad-events-queue");
        assertThat(sent.messageBody()).contains("CAMPAIGN_CREATED");
        assertThat(sent.messageBody()).contains("42");
        assertThat(event.getEventId()).isNotNull();
    }
}

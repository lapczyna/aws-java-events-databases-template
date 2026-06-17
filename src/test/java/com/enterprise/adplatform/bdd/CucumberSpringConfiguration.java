package com.enterprise.adplatform.bdd;

import com.enterprise.adplatform.AdPlatformApplication;
import com.enterprise.adplatform.messaging.kafka.KafkaProducerService;
import com.enterprise.adplatform.messaging.sqs.SqsConsumerService;
import com.enterprise.adplatform.messaging.sqs.SqsProducerService;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
        classes = AdPlatformApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    // Messaging beans are mocked so no SQS/Kafka infrastructure is required in CI.
    // SqsConsumerService is mocked to prevent its @Scheduled poller from running.
    @MockBean SqsProducerService sqsProducerService;
    @MockBean SqsConsumerService sqsConsumerService;
    @MockBean KafkaProducerService kafkaProducerService;
}

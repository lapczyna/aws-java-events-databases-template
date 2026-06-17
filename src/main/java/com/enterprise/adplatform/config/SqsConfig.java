package com.enterprise.adplatform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class SqsConfig {

    private final Region awsRegion;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final URI awsEndpointOverride;

    @Bean
    public SqsClient sqsClient() {
        SqsClientBuilder builder = SqsClient.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider);

        if (awsEndpointOverride != null) {
            builder.endpointOverride(awsEndpointOverride);
        }

        return builder.build();
    }
}

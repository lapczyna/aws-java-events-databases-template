package com.enterprise.adplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.localstack.enabled:false}")
    private boolean localstackEnabled;

    @Value("${aws.localstack.endpoint:http://localhost:4566}")
    private String localstackEndpoint;

    @Value("${aws.localstack.access-key:test}")
    private String localstackAccessKey;

    @Value("${aws.localstack.secret-key:test}")
    private String localstackSecretKey;

    @Bean
    public Region awsRegion() {
        return Region.of(awsRegion);
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (localstackEnabled) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(localstackAccessKey, localstackSecretKey));
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public URI awsEndpointOverride() {
        if (localstackEnabled) {
            return URI.create(localstackEndpoint);
        }
        return null;
    }
}

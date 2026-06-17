package com.enterprise.adplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdPlatformApplication.class, args);
    }
}

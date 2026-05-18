package com.vg.task.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.academic-url}")
    private String academicServiceUrl;

    @Value("${services.student-url}")
    private String studentServiceUrl;

    @Bean
    public WebClient academicWebClient() {
        return WebClient.builder()
                .baseUrl(academicServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient studentWebClient() {
        return WebClient.builder()
                .baseUrl(studentServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

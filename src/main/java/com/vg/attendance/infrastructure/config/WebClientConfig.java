package com.vg.attendance.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${schedule.service.url:http://vg-ms-schedule:5083}")
    private String scheduleServiceUrl;

    @Value("${enrollment.service.url:http://vg-ms-enrollment:5087}")
    private String enrollmentServiceUrl;

    @Value("${user.service.url:http://vg-ms-user:5082}")
    private String userServiceUrl;

    @Value("${services.comms.url:http://vg-ms-comms:5088}")
    private String commsServiceUrl;

    @Bean
    public WebClient scheduleWebClient() {
        return WebClient.builder()
                .baseUrl(scheduleServiceUrl)
                .build();
    }

    @Bean
    public WebClient enrollmentWebClient() {
        return WebClient.builder()
                .baseUrl(enrollmentServiceUrl)
                .build();
    }

    @Bean
    public WebClient userWebClient() {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    public WebClient commsWebClient() {
        return WebClient.builder()
                .baseUrl(commsServiceUrl)
                .build();
    }
}

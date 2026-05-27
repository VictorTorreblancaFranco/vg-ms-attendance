package com.vg.task.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CustomHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient academicWebClient;
    private final WebClient studentWebClient;

    public CustomHealthIndicator(
            @Value("${services.academic-url:http://localhost:8082}") String academicUrl,
            @Value("${services.student-url:http://localhost:8083}") String studentUrl) {
        this.academicWebClient = WebClient.builder().baseUrl(academicUrl).build();
        this.studentWebClient = WebClient.builder().baseUrl(studentUrl).build();
    }

    @Override
    public Mono<Health> health() {
        return checkAcademicService()
                .zipWith(checkStudentService())
                .map(tuple -> Health.up()
                        .withDetail("academic-service", tuple.getT1() ? "UP" : "DOWN")
                        .withDetail("student-service", tuple.getT2() ? "UP" : "DOWN")
                        .build())
                .onErrorReturn(Health.down().withDetail("error", "Health check failed").build());
    }

    private Mono<Boolean> checkAcademicService() {
        return academicWebClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorReturn(false);
    }

    private Mono<Boolean> checkStudentService() {
        return studentWebClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .onErrorReturn(false);
    }
}

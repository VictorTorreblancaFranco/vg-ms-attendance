package com.vg.attendance.infrastructure.adapter.out.client;

import com.vg.attendance.infrastructure.adapter.out.client.dto.ScheduleResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Component
public class ScheduleClient {

    private final WebClient scheduleWebClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    @Autowired
    public ScheduleClient(@Qualifier("scheduleWebClient") WebClient scheduleWebClient,
                          CircuitBreakerRegistry circuitBreakerRegistry,
                          RetryRegistry retryRegistry) {
        this.scheduleWebClient = scheduleWebClient;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("scheduleService");
        this.retry = retryRegistry.retry("scheduleService");
        log.info("ScheduleClient initialized with CircuitBreaker and Retry");
    }

    public Flux<ScheduleResponse> getTodayClassesByTeacher(String teacherId, String authHeader) {
        log.info("Obteniendo clases de hoy para el profesor: {}", teacherId);
        return scheduleWebClient.get()
                .uri("/schedules/today/teacher/{teacherId}", teacherId)
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToFlux(ScheduleResponse.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(e -> new RuntimeException("Servicio no disponible por el momento, intente más tarde", e));
    }

    public Flux<ScheduleResponse> getClassesByTeacher(String teacherId, String authHeader) {
        log.info("Obteniendo todas las clases del profesor: {}", teacherId);
        return scheduleWebClient.get()
                .uri("/schedules/teacher/{teacherId}", teacherId)
                .header("Authorization", authHeader)
                .retrieve()
                .bodyToFlux(ScheduleResponse.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(e -> new RuntimeException("Servicio no disponible por el momento, intente más tarde", e));
    }
}

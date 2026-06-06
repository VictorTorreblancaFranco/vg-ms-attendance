package com.vg.attendance.infrastructure.adapter.out.client;

import com.vg.attendance.infrastructure.adapter.out.client.dto.EnrollmentResponse;
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
public class EnrollmentClient {

    private final WebClient enrollmentWebClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    @Autowired
    public EnrollmentClient(@Qualifier("enrollmentWebClient") WebClient enrollmentWebClient,
                            CircuitBreakerRegistry circuitBreakerRegistry,
                            RetryRegistry retryRegistry) {
        this.enrollmentWebClient = enrollmentWebClient;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("enrollmentService");
        this.retry = retryRegistry.retry("enrollmentService");
    }

    public Flux<EnrollmentResponse> getStudentsByGradeSectionYear(Long gradeId, Long sectionId, Long yearId, String token) {
        log.info("Obteniendo alumnos para grado: {}, sección: {}, año: {}", gradeId, sectionId, yearId);
        return enrollmentWebClient.get()
                .uri("/api/enrollments/grade/{gradeId}/section/{sectionId}/year/{yearId}", gradeId, sectionId, yearId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToFlux(EnrollmentResponse.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(e -> new RuntimeException("Servicio no disponible por el momento, intente más tarde", e));
    }
}

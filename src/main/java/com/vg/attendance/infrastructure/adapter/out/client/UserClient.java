package com.vg.attendance.infrastructure.adapter.out.client;

import com.vg.attendance.infrastructure.adapter.out.client.dto.ParentStudentLinkResponse;
import com.vg.attendance.infrastructure.adapter.out.client.dto.UserResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Component
public class UserClient {

    private final WebClient userWebClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    @Autowired
    public UserClient(@Qualifier("userWebClient") WebClient userWebClient,
                      CircuitBreakerRegistry circuitBreakerRegistry,
                      RetryRegistry retryRegistry) {
        this.userWebClient = userWebClient;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("userService");
        this.retry = retryRegistry.retry("userService");
    }

    public Mono<UserResponse> getUserById(String userId) {
        log.info("Obteniendo usuario: {}", userId);
        return userWebClient.get()
                .uri("/api/users/{id}", userId)
                .header("X-Internal-Request", "gateway")
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }
                    if (response.statusCode().isError()) {
                        return response.createException().flatMap(Mono::error);
                    }
                    return response.bodyToMono(UserResponse.class);
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    log.error("Error al obtener usuario {}: {}", userId, e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<UserResponse> getUserById(String userId, String token) {
        log.info("Obteniendo usuario con token: {}", userId);
        return userWebClient.get()
                .uri("/api/users/{id}", userId)
                .header("Authorization", "Bearer " + token)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }
                    if (response.statusCode().isError()) {
                        return response.createException().flatMap(Mono::error);
                    }
                    return response.bodyToMono(UserResponse.class);
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    log.error("Error al obtener usuario {}: {}", userId, e.getMessage());
                    return Mono.empty();
                });
    }

    public Flux<ParentStudentLinkResponse> getGuardiansByStudentId(String studentId) {
        log.info("Obteniendo apoderados del estudiante: {}", studentId);
        return userWebClient.get()
                .uri("/api/users/students/{id}/guardians", studentId)
                .header("X-Internal-Request", "gateway")
                .exchangeToFlux(response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Flux.empty();
                    }
                    if (response.statusCode().isError()) {
                        return response.createException().flatMapMany(Flux::error);
                    }
                    return response.bodyToFlux(ParentStudentLinkResponse.class);
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    log.error("Error al obtener apoderados del estudiante {}: {}", studentId, e.getMessage());
                    return Flux.empty();
                });
    }

}

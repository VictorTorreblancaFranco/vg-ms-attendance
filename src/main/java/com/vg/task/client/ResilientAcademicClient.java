package com.vg.task.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResilientAcademicClient {
    
    private final AcademicClient academicClient;
    
    @Retry(name = "academicService", fallbackMethod = "validateClassFallback")
    @CircuitBreaker(name = "academicService", fallbackMethod = "validateClassFallback")
    public Mono<Boolean> validateClassWithRetry(Integer classId) {
        log.info("🔄 Intentando validar clase {} (con reintentos y circuit breaker)", classId);
        return academicClient.validateClass(classId);
    }
    
    private Mono<Boolean> validateClassFallback(Integer classId, Throwable t) {
        log.warn("⚠️ Fallback activado para clase {} después de {} reintentos. Causa: {}", 
                 classId, 5, t.getMessage());
        
        // Decisión: si el circuito está abierto o fallaron los reintentos,
        // asumimos que la clase NO es válida para no crear tareas huérfanas
        return Mono.just(false);
    }
}

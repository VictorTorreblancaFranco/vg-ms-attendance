package com.vg.task.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RateLimitService {
    
    // Configuración: máximo 10 peticiones por segundo por IP
    private static final int MAX_REQUESTS_PER_SECOND = 10;
    private static final int TIME_WINDOW_SECONDS = 1;
    
    // Estructura para almacenar contadores por IP
    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();
    
    public Mono<Boolean> allowRequest(String clientIp) {
        if (clientIp == null || clientIp.equals("unknown")) {
            clientIp = "unknown";
        }
        
        RateLimitInfo info = requestCounts.computeIfAbsent(clientIp, 
            k -> new RateLimitInfo(0, Instant.now()));
        
        // Limpiar registros antiguos (más de 5 segundos sin actividad)
        cleanupOldEntries();
        
        // Verificar si debemos reiniciar el contador (pasó la ventana de tiempo)
        Instant now = Instant.now();
        if (now.isAfter(info.windowStart.plusSeconds(TIME_WINDOW_SECONDS))) {
            // Nueva ventana de tiempo, reiniciar contador
            info.count = 1;
            info.windowStart = now;
            log.debug("🔄 Nueva ventana para IP {}: contador=1", clientIp);
            return Mono.just(true);
        }
        
        // Dentro de la misma ventana, verificar límite
        if (info.count < MAX_REQUESTS_PER_SECOND) {
            info.count++;
            log.debug("✅ IP {}: petición {} de {}", clientIp, info.count, MAX_REQUESTS_PER_SECOND);
            return Mono.just(true);
        }
        
        // Límite excedido
        log.warn("🚫 IP {} excedió el límite de {} peticiones por segundo", clientIp, MAX_REQUESTS_PER_SECOND);
        return Mono.just(false);
    }
    
    private void cleanupOldEntries() {
        Instant now = Instant.now();
        requestCounts.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().windowStart.plusSeconds(5)));
    }
    
    // Clase para almacenar información de rate limiting
    private static class RateLimitInfo {
        int count;
        Instant windowStart;
        
        RateLimitInfo(int count, Instant windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}

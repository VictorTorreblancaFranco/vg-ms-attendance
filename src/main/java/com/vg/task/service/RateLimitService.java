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

    private static final Map<String, Integer> LIMITS = Map.of(
            "/api/v1/task/save", 20,
            "/api/v1/task/update", 20,
            "/api/v1/submissions/submit", 30,
            "/api/v1/submissions/bulk-grade", 5,
            "default", 50
    );

    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();

    public Mono<Boolean> allowRequest(String userId, String path) {
        if (userId == null || userId.equals("anonymous")) {
            userId = "anonymous";
        }

        int limit = LIMITS.getOrDefault(getEndpointKey(path), LIMITS.get("default"));
        String key = userId + ":" + getEndpointKey(path);

        RateLimitInfo info = requestCounts.computeIfAbsent(key,
                k -> new RateLimitInfo(0, Instant.now(), limit));

        if (info.limit != limit) {
            info.limit = limit;
        }

        cleanupOldEntries();

        Instant now = Instant.now();
        if (now.isAfter(info.windowStart.plusSeconds(60))) {
            info.count = 1;
            info.windowStart = now;
            log.debug("🔄 Nueva ventana para {}", key);
            return Mono.just(true);
        }

        if (info.count < info.limit) {
            info.count++;
            log.debug("✅ {}: petición {} de {}", key, info.count, info.limit);
            return Mono.just(true);
        }

        log.warn("🚫 {} excedió el límite de {} peticiones por minuto", key, info.limit);
        return Mono.just(false);
    }

    private String getEndpointKey(String path) {
        if (path.contains("/save")) return "/api/v1/task/save";
        if (path.contains("/update")) return "/api/v1/task/update";
        if (path.contains("/submit")) return "/api/v1/submissions/submit";
        if (path.contains("/bulk-grade")) return "/api/v1/submissions/bulk-grade";
        return path;
    }

    private void cleanupOldEntries() {
        Instant now = Instant.now();
        requestCounts.entrySet().removeIf(entry ->
                now.isAfter(entry.getValue().windowStart.plusSeconds(120)));
    }

    private static class RateLimitInfo {
        int count;
        Instant windowStart;
        int limit;

        RateLimitInfo(int count, Instant windowStart, int limit) {
            this.count = count;
            this.windowStart = windowStart;
            this.limit = limit;
        }
    }
}

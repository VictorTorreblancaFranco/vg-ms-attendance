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
            "/api/v1/task", 20,
            "/api/v1/task/paged", 20,
            "/api/v1/task/save", 5,
            "/api/v1/task/update", 5,
            "/api/v1/task/export/csv", 3,
            "/api/v1/task/export/excel", 3,
            "/api/v1/submissions/submit", 3,
            "/api/v1/upload/task", 2,
            "default", 10
    );

    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();
    private final MetricsService metricsService;

    public RateLimitService(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    public Mono<Boolean> allowRequest(String clientIp, String path) {
        if (clientIp == null || clientIp.equals("unknown")) {
            clientIp = "unknown";
        }

        int limit = LIMITS.getOrDefault(getEndpointKey(path), LIMITS.get("default"));
        String key = clientIp + ":" + getEndpointKey(path);

        RateLimitInfo info = requestCounts.computeIfAbsent(key,
                k -> new RateLimitInfo(0, Instant.now(), limit));

        if (info.limit != limit) {
            info.limit = limit;
        }

        cleanupOldEntries();

        Instant now = Instant.now();
        if (now.isAfter(info.windowStart.plusSeconds(1))) {
            info.count = 1;
            info.windowStart = now;
            log.debug("🔄 Nueva ventana para {}: contador=1", key);
            return Mono.just(true);
        }

        if (info.count < info.limit) {
            info.count++;
            log.debug("✅ {}: petición {} de {}", key, info.count, info.limit);
            return Mono.just(true);
        }

        log.warn("🚫 {} excedió el límite de {} peticiones por segundo", key, info.limit);
        metricsService.recordRateLimitBlock(clientIp, getEndpointKey(path));
        return Mono.just(false);
    }

    private String getEndpointKey(String path) {
        if (path.contains("/export/csv")) return "/api/v1/task/export/csv";
        if (path.contains("/export/excel")) return "/api/v1/task/export/excel";
        if (path.contains("/save")) return "/api/v1/task/save";
        if (path.contains("/update")) return "/api/v1/task/update";
        if (path.contains("/paged")) return "/api/v1/task/paged";
        if (path.contains("/submit")) return "/api/v1/submissions/submit";
        if (path.contains("/upload")) return "/api/v1/upload/task";
        return path;
    }

    private void cleanupOldEntries() {
        Instant now = Instant.now();
        requestCounts.entrySet().removeIf(entry ->
                now.isAfter(entry.getValue().windowStart.plusSeconds(5)));
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

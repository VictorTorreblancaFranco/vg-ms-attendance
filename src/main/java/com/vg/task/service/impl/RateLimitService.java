package com.vg.task.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RateLimitService {

    private static final Map<String, Integer> ENDPOINT_LIMITS = Map.of(
            "save-task", 10,
            "update-task", 10,
            "submit-submission", 15,
            "grade-submission", 20,
            "bulk-grade", 5,
            "create-attendance", 30,
            "export-data", 5,
            "default", 50
    );

    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();

    public Mono<Boolean> allowRequest(String clientIp, String path) {
        String endpointKey = getEndpointKey(path);
        int limit = ENDPOINT_LIMITS.getOrDefault(endpointKey, ENDPOINT_LIMITS.get("default"));
        String key = clientIp + ":" + endpointKey;

        RateLimitInfo info = requestCounts.computeIfAbsent(key,
                k -> new RateLimitInfo(0, Instant.now(), limit));

        cleanupOldEntries();

        Instant now = Instant.now();
        if (now.isAfter(info.windowStart.plusSeconds(60))) {
            info.count = 1;
            info.windowStart = now;
            return Mono.just(true);
        }

        if (info.count < info.limit) {
            info.count++;
            return Mono.just(true);
        }

        log.warn("🚫 Rate limit exceeded for {}: {}/{} requests per minute", key, info.count, info.limit);
        return Mono.just(false);
    }

    private String getEndpointKey(String path) {
        if (path.contains("/task") && path.contains("/save")) return "save-task";
        if (path.contains("/task") && path.contains("/update")) return "update-task";
        if (path.contains("/submissions/submit")) return "submit-submission";
        if (path.contains("/submissions") && path.contains("/grade")) return "grade-submission";
        if (path.contains("/submissions/bulk-grade")) return "bulk-grade";
        if (path.contains("/attendance") && !path.contains("/export")) return "create-attendance";
        if (path.contains("/export")) return "export-data";
        return "default";
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

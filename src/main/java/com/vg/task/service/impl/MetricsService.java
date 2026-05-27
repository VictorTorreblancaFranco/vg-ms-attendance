package com.vg.task.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void recordTaskCreated() {
        meterRegistry.counter("tasks.created.total").increment();
    }

    public void recordTaskPublished() {
        meterRegistry.counter("tasks.published.total").increment();
    }

    public void recordSubmissionCreated() {
        meterRegistry.counter("submissions.created.total").increment();
    }

    public void recordSubmissionGraded() {
        meterRegistry.counter("submissions.graded.total").increment();
    }

    public void recordValidationTime(long milliseconds, String service) {
        Timer.builder("validation.time")
                .tag("service", service)
                .register(meterRegistry)
                .record(Duration.ofMillis(milliseconds));
    }

    public void recordRateLimitBlock(String clientIp, String endpoint) {
        meterRegistry.counter("rate.limit.blocks", "endpoint", endpoint).increment();
    }
}

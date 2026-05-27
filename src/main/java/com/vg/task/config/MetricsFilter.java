package com.vg.task.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MetricsFilter implements WebFilter {

    private final MeterRegistry meterRegistry;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Instant start = Instant.now();
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long duration = Duration.between(start, Instant.now()).toMillis();
                    int status = exchange.getResponse().getStatusCode() != null ? 
                                 exchange.getResponse().getStatusCode().value() : 500;

                    Timer.builder("http.server.requests")
                            .tag("method", method)
                            .tag("uri", path)
                            .tag("status", String.valueOf(status))
                            .register(meterRegistry)
                            .record(Duration.ofMillis(duration));
                });
    }
}

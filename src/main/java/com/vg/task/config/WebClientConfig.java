package com.vg.task.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${services.academic-url}")
    private String academicServiceUrl;

    @Value("${services.student-url}")
    private String studentServiceUrl;

    private HttpClient createHttpClient() {
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(50)
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .build();

        return HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)))
                .responseTimeout(Duration.ofSeconds(15));
    }

    @Bean
    public WebClient academicWebClient() {
        return WebClient.builder()
                .baseUrl(academicServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(createHttpClient()))
                .filter((request, next) -> {
                    String correlationId = MDC.get("correlationId");
                    if (correlationId != null) {
                        return next.exchange(
                                ClientRequest.from(request)
                                        .header("X-Correlation-Id", correlationId)
                                        .build()
                        );
                    }
                    return next.exchange(request);
                })
                .build();
    }

    @Bean
    public WebClient studentWebClient() {
        return WebClient.builder()
                .baseUrl(studentServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .clientConnector(new ReactorClientHttpConnector(createHttpClient()))
                .filter((request, next) -> {
                    String correlationId = MDC.get("correlationId");
                    if (correlationId != null) {
                        return next.exchange(
                                ClientRequest.from(request)
                                        .header("X-Correlation-Id", correlationId)
                                        .build()
                        );
                    }
                    return next.exchange(request);
                })
                .build();
    }
}

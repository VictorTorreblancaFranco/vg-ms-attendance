package com.vg.task.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StudentClient {
    private final WebClient webClient;

    public StudentClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8083").build();
    }

    public Mono<Boolean> validarEstudiante(Long studentId) {
        return webClient.get()
                .uri("/api/students/{id}", studentId)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }
}

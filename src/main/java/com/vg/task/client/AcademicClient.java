package com.vg.task.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AcademicClient {
    private final WebClient webClient;

    public AcademicClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    public Mono<Boolean> validarClase(Integer classId) {
        return webClient.get()
                .uri("/api/clases/{id}", classId)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }
}

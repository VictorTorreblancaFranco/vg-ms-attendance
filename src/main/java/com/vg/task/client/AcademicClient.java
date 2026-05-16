package com.vg.task.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AcademicClient {

    private final WebClient academicWebClient;

    public AcademicClient(@Qualifier("academicWebClient") WebClient academicWebClient) {
        this.academicWebClient = academicWebClient;
    }

    public Mono<Boolean> validateClass(Integer classId) {
        return academicWebClient.get()
                .uri("/api/clases/{id}", classId)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }
}

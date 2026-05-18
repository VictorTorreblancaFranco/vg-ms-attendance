package com.vg.task.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StudentClient {

    private final WebClient studentWebClient;

    public StudentClient(@Qualifier("studentWebClient") WebClient studentWebClient) {
        this.studentWebClient = studentWebClient;
    }

    public Mono<Boolean> validateStudent(Integer studentId) {
        return studentWebClient.get()
                .uri("/api/students/{id}", studentId)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorReturn(false);
    }
}

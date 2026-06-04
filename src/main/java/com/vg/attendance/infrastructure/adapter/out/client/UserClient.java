package com.vg.attendance.infrastructure.adapter.out.client;

import com.vg.attendance.infrastructure.adapter.out.client.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserClient {

    @Qualifier("userWebClient")
    private final WebClient userWebClient;

    public Mono<UserResponse> getUserById(String userId) {
        log.info("Obteniendo usuario: {}", userId);
        return userWebClient.get()
                .uri("/api/users/{id}", userId)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(e -> {
                    log.error("Error al obtener usuario {}: {}", userId, e.getMessage());
                    return Mono.empty();
                });
    }
}

package com.vg.attendance.infrastructure.adapter.out.client;

import com.vg.attendance.infrastructure.adapter.out.client.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserClient {

    private final WebClient userWebClient;

    @Autowired
    public UserClient(@Qualifier("userWebClient") WebClient userWebClient) {
        this.userWebClient = userWebClient;
    }

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

    public Mono<UserResponse> getUserById(String userId, String token) {
        log.info("Obteniendo usuario con token: {}", userId);
        return userWebClient.get()
                .uri("/api/users/{id}", userId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(e -> {
                    log.error("Error al obtener usuario {}: {}", userId, e.getMessage());
                    String shortId = userId.length() > 4 ? userId.substring(userId.length() - 4) : userId;
                    UserResponse fallback = new UserResponse();
                    fallback.setId(userId);
                    fallback.setFirstName("Alumno");
                    fallback.setLastName(shortId);
                    fallback.setEmail("");
                    return Mono.just(fallback);
                });
    }
}

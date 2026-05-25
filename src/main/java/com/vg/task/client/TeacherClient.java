package com.vg.task.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeacherClient {

    @Qualifier("studentWebClient")
    private final WebClient studentWebClient;

    public Flux<TeacherInfo> getAllTeachers() {
        log.info("👨‍🏫 Obteniendo todos los profesores desde student-service");
        return studentWebClient.get()
                .uri("/api/teachers")
                .retrieve()
                .bodyToFlux(TeacherResponse.class)
                .map(this::toTeacherInfo)
                .doOnNext(t -> log.info("👨‍🏫 Profesor: {} - {}", t.id(), t.teacherCode()))
                .onErrorResume(e -> {
                    log.error("❌ Error obteniendo profesores: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Mono<TeacherInfo> getTeacherById(Long id) {
        log.info("👨‍🏫 Obteniendo profesor por ID: {}", id);
        return studentWebClient.get()
                .uri("/api/teachers/{id}", id)
                .retrieve()
                .bodyToMono(TeacherResponse.class)
                .map(this::toTeacherInfo)
                .onErrorResume(e -> {
                    log.error("❌ Error obteniendo profesor {}: {}", id, e.getMessage());
                    return Mono.empty();
                });
    }

    private TeacherInfo toTeacherInfo(TeacherResponse response) {
        return new TeacherInfo(
                response.id(),
                response.personId(),
                response.userId(),
                response.teacherCode(),
                response.specialty(),
                response.professionalTitle(),
                response.isActive()
        );
    }

    // Clase para mapear la respuesta del student-service
    public record TeacherResponse(
        Long id,
        Long personId,
        Long userId,
        String teacherCode,
        String specialty,
        String professionalTitle,
        Boolean isActive
    ) {}

    public record TeacherInfo(Long id, Long personId, Long userId, String teacherCode,
                              String specialty, String professionalTitle, Boolean isActive) {}
}

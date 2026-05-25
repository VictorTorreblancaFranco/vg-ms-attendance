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
public class StudentClient {

    @Qualifier("studentWebClient")
    private final WebClient studentWebClient;

    // Para AttendanceServiceImpl - valida si existe
    public Mono<Boolean> validateStudent(Integer studentId) {
        log.info("🔍 Validando estudiante {} contra student-service", studentId);
        return studentWebClient.get()
                .uri("/api/students/{id}", studentId)
                .retrieve()
                .bodyToMono(StudentResponse.class)
                .map(s -> true)
                .onErrorReturn(false);
    }

    // Para AttendanceServiceImpl - obtiene información del estudiante
    public Mono<StudentInfo> getStudentInfo(Integer studentId) {
        log.info("📚 Obteniendo información del estudiante {}", studentId);
        return studentWebClient.get()
                .uri("/api/students/{id}", studentId)
                .retrieve()
                .bodyToMono(StudentResponse.class)
                .map(this::toStudentInfo)
                .onErrorResume(e -> {
                    log.error("Error obteniendo estudiante {}: {}", studentId, e.getMessage());
                    return Mono.empty();
                });
    }

    // Para UserSelectionController - obtiene todos los estudiantes
    public Flux<StudentInfo> getAllStudents() {
        log.info("📚 Obteniendo todos los estudiantes desde student-service");
        return studentWebClient.get()
                .uri("/api/students")
                .retrieve()
                .bodyToFlux(StudentResponse.class)
                .map(this::toStudentInfo)
                .onErrorResume(e -> {
                    log.error("❌ Error obteniendo estudiantes: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    private StudentInfo toStudentInfo(StudentResponse response) {
        return new StudentInfo(
                response.id(),
                response.studentCode(),
                response.studentCode(),
                response.isActive()
        );
    }

    public record StudentResponse(
        Integer id,
        String studentCode,
        String enrollmentNumber,
        Boolean isActive
    ) {}

    public record StudentInfo(Integer id, String studentCode, String nombre, Boolean active) {}
}

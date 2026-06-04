package com.vg.attendance.infrastructure.adapter.out.client;

import com.vg.attendance.infrastructure.adapter.out.client.dto.EnrollmentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class EnrollmentClient {

    private final WebClient enrollmentWebClient;

    @Autowired
    public EnrollmentClient(@Qualifier("enrollmentWebClient") WebClient enrollmentWebClient) {
        this.enrollmentWebClient = enrollmentWebClient;
    }

    public Flux<EnrollmentResponse> getStudentsByGradeSectionYear(Long gradeId, Long sectionId, Long yearId, String token) {
        log.info("Obteniendo alumnos para grado: {}, sección: {}, año: {}", gradeId, sectionId, yearId);
        return enrollmentWebClient.get()
                .uri("/api/enrollments/grade/{gradeId}/section/{sectionId}/year/{yearId}", gradeId, sectionId, yearId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToFlux(EnrollmentResponse.class)
                .onErrorResume(e -> {
                    log.error("Error al obtener alumnos: {}", e.getMessage());
                    return Flux.empty();
                });
    }
}

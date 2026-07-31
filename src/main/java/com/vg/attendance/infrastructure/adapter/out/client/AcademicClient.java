package com.vg.attendance.infrastructure.adapter.out.client;

import com.vg.attendance.infrastructure.adapter.out.client.dto.CourseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AcademicClient {

    private final WebClient academicWebClient;

    @Autowired
    public AcademicClient(@Qualifier("academicWebClient") WebClient academicWebClient) {
        this.academicWebClient = academicWebClient;
    }

    public Mono<CourseResponse> getCourseById(Long id) {
        return academicWebClient.get()
                .uri("/api/course/courses/{id}", id)
                .header("X-Internal-Request", "gateway")
                .retrieve()
                .bodyToMono(CourseResponse.class)
                .onErrorResume(error -> {
                    log.warn("No se pudo obtener curso {}: {}", id, error.getMessage());
                    return Mono.empty();
                });
    }
}

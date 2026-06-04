package com.vg.attendance.infrastructure.adapter.out.client;

import com.vg.attendance.infrastructure.adapter.out.client.dto.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleClient {

    @Qualifier("scheduleWebClient")
    private final WebClient scheduleWebClient;

    public Flux<ScheduleResponse> getTodayClassesByTeacher(String teacherId) {
        log.info("Obteniendo clases de hoy para el profesor: {}", teacherId);
        return scheduleWebClient.get()
                .uri("/schedules/today/teacher/{teacherId}", teacherId)
                .retrieve()
                .bodyToFlux(ScheduleResponse.class)
                .onErrorResume(e -> {
                    log.error("Error al obtener clases del profesor {}: {}", teacherId, e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<ScheduleResponse> getClassesByTeacher(String teacherId) {
        log.info("Obteniendo todas las clases del profesor: {}", teacherId);
        return scheduleWebClient.get()
                .uri("/schedules/teacher/{teacherId}", teacherId)
                .retrieve()
                .bodyToFlux(ScheduleResponse.class)
                .onErrorResume(e -> {
                    log.error("Error al obtener clases del profesor {}: {}", teacherId, e.getMessage());
                    return Flux.empty();
                });
    }
}

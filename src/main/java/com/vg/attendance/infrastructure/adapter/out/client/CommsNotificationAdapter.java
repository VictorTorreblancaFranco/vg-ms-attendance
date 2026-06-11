package com.vg.attendance.infrastructure.adapter.out.client;

import com.vg.attendance.application.port.out.NotificationPort;
import com.vg.attendance.domain.model.Attendance;
import com.vg.attendance.infrastructure.adapter.out.client.dto.ParentStudentLinkResponse;
import com.vg.attendance.infrastructure.adapter.out.client.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class CommsNotificationAdapter implements NotificationPort {

    private final WebClient commsWebClient;
    private final UserClient userClient;

    public CommsNotificationAdapter(
            @Qualifier("commsWebClient") WebClient commsWebClient,
            UserClient userClient
    ) {
        this.commsWebClient = commsWebClient;
        this.userClient = userClient;
    }

    @Override
    public Mono<Void> notifyAbsenceOrLate(Attendance attendance) {
        String templateId = templateFor(attendance.getEstado());
        if (templateId == null) {
            return Mono.empty();
        }

        return userClient.getGuardiansByStudentId(attendance.getEstudianteId())
                .next()
                .flatMap(link -> buildRequest(attendance, link, templateId))
                .flatMap(this::sendNotification)
                .onErrorResume(error -> {
                    log.warn("No se pudo notificar asistencia a comms: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> notifyThreeFullAbsenceDays(String estudianteId, int absenceDays) {
        return Mono.empty();
    }

    private Mono<Map<String, Object>> buildRequest(
            Attendance attendance,
            ParentStudentLinkResponse link,
            String templateId
    ) {
        return userClient.getUserById(attendance.getEstudianteId())
                .defaultIfEmpty(UserResponse.builder().id(attendance.getEstudianteId()).build())
                .map(student -> {
                    Map<String, Object> variables = new LinkedHashMap<>();
                    variables.put("alumno", fullName(student));
                    variables.put("fecha", attendance.getFecha().toString());
                    variables.put("curso", "Clase " + attendance.getClaseId());
                    variables.put("justificacion", attendance.getJustificacionNota() == null
                            ? ""
                            : attendance.getJustificacionNota());

                    Map<String, Object> request = new LinkedHashMap<>();
                    request.put("destinatarioId", link.getParentId());
                    request.put("canal", "ambos");
                    request.put("plantillaId", templateId);
                    request.put("variables", variables);
                    return request;
                });
    }

    private Mono<Void> sendNotification(Map<String, Object> request) {
        return commsWebClient.post()
                .uri("/api/notifications/send")
                .header("X-Internal-Request", "gateway")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("Notificación de asistencia enviada a comms"))
                .then();
    }

    private String templateFor(String status) {
        if (status == null) {
            return null;
        }

        return switch (status.toUpperCase()) {
            case "F" -> "TPL_001";
            case "T" -> "TPL_002";
            case "J" -> "TPL_003";
            default -> null;
        };
    }

    private String fullName(UserResponse user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? "Estudiante" : fullName;
    }
}

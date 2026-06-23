package com.vg.attendance.infrastructure.config;

import com.vg.attendance.domain.exception.BusinessException;
import com.vg.attendance.domain.exception.ConflictException;
import com.vg.attendance.domain.exception.ForbiddenException;
import com.vg.attendance.domain.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionUsesStableErrorContract() {
        StepVerifier.create(handler.handleBadRequestException(new BusinessException("La tardanza requiere hora de llegada")))
            .assertNext(response -> {
                assertThat(response).containsEntry("code", "BUSINESS_RULE_VIOLATION");
                assertThat(response).containsEntry("message", "La tardanza requiere hora de llegada");
                assertThat(response).containsEntry("status", 400);
                assertThat(response).containsKeys("details", "timestamp", "error");
            })
            .verifyComplete();
    }

    @Test
    void knownExceptionsExposeFrontendFriendlyCodes() {
        StepVerifier.create(handler.handleNotFoundException(new NotFoundException("Asistencia no encontrada")))
            .assertNext(response -> assertThat(response).containsEntry("code", "NOT_FOUND"))
            .verifyComplete();

        StepVerifier.create(handler.handleConflictException(new ConflictException("Ya existe asistencia")))
            .assertNext(response -> assertThat(response).containsEntry("code", "ATTENDANCE_ALREADY_EXISTS"))
            .verifyComplete();

        StepVerifier.create(handler.handleForbiddenException(new ForbiddenException("No tienes permisos")))
            .assertNext(response -> assertThat(response).containsEntry("code", "FORBIDDEN"))
            .verifyComplete();
    }
}

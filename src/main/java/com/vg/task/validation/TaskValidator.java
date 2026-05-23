package com.vg.task.validation;

import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class TaskValidator {

    public void validateTaskRequest(TaskRequestDTO request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new BadRequestException("El título es requerido");
        }
        if (request.classId() == null) {
            throw new BadRequestException("El ID de clase es requerido");
        }
        if (request.dueDate() == null) {
            throw new BadRequestException("La fecha de entrega es requerida");
        }
        if (request.dueDate().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("La fecha de entrega no puede ser en el pasado");
        }
        if (request.createdBy() == null) {
            throw new BadRequestException("El creador es requerido");
        }
        if (request.pointsValue() != null && request.pointsValue() < 0) {
            throw new BadRequestException("El puntaje no puede ser negativo");
        }
        if (request.scheduledPublishDate() != null && 
            request.scheduledPublishDate().isAfter(request.dueDate())) {
            throw new BadRequestException("La fecha de publicación debe ser antes de la entrega");
        }
        if (request.scheduledCloseDate() != null && 
            request.scheduledCloseDate().isBefore(request.dueDate())) {
            throw new BadRequestException("La fecha de cierre no puede ser antes de la entrega");
        }
    }

    public void validateGradeRequest(GradeRequestDTO request) {
        if (request.grade() == null) {
            throw new BadRequestException("La nota es requerida");
        }
        if (request.grade() < 0 || request.grade() > 20) {
            throw new BadRequestException("La nota debe estar entre 0 y 20");
        }
        if (request.gradedBy() == null) {
            throw new BadRequestException("El calificador es requerido");
        }
        if (request.isLate() != null && request.isLate()) {
            if (request.justification() == null || request.justification().isBlank()) {
                throw new BadRequestException("Debe proporcionar justificación para entrega tardía");
            }
            if (request.justification().length() < 10) {
                throw new BadRequestException("La justificación debe tener al menos 10 caracteres");
            }
        }
    }
}

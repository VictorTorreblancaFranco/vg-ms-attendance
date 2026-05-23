package com.vg.task.util;

import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.exception.BadRequestException;

import java.time.OffsetDateTime;

public class ValidationUtils {

    private ValidationUtils() {}

    public static void validateTaskRequest(TaskRequestDTO request) {
        if (request.dueDate() == null) {
            throw new BadRequestException("Due date is required");
        }

        OffsetDateTime now = OffsetDateTime.now();
        
        if (request.dueDate().isBefore(now)) {
            throw new BadRequestException("Due date cannot be in the past");
        }
        
        if (request.scheduledPublishDate() != null && 
            request.scheduledPublishDate().isAfter(request.dueDate())) {
            throw new BadRequestException("Publish date must be before due date");
        }
        
        if (request.scheduledCloseDate() != null && 
            request.scheduledCloseDate().isBefore(request.dueDate())) {
            throw new BadRequestException("Close date cannot be before due date");
        }
        
        if (request.pointsValue() != null && request.pointsValue() < 0) {
            throw new BadRequestException("Points value cannot be negative");
        }
        
        // ❌ ELIMINADA validación de archivos obligatorios
        // Los profesores pueden crear tareas sin adjuntar archivos
    }
}

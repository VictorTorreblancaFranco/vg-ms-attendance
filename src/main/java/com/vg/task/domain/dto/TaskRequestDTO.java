package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record TaskRequestDTO(
    @NotBlank(message = "Title is required")
    String title,
    
    String description,
    String instructions,  // Aquí va el resumen de clase o instrucciones
    
    @NotNull(message = "Class ID is required")
    Integer classId,
    
    Integer criterionId,
    
    Double pointsValue,
    
    @NotNull(message = "Due date is required")
    OffsetDateTime dueDate,
    
    OffsetDateTime scheduledPublishDate,
    
    OffsetDateTime scheduledCloseDate,
    
    @NotNull(message = "Created by is required")
    Integer createdBy,
    
    String status
    
    // ❌ Eliminado: List<TaskFileDTO> files - Los recursos se suben aparte
) {}

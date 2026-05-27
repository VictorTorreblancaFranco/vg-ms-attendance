package com.vg.task.domain.dto;

import java.time.OffsetDateTime;

public record TaskResponseDTO(
    Long id,
    String title,
    String description,
    String instructions,
    Integer classId,
    Integer criterionId,
    Double pointsValue,
    OffsetDateTime dueDate,
    OffsetDateTime scheduledPublishDate,
    OffsetDateTime scheduledCloseDate,
    String status,
    Boolean isDeleted,
    Integer createdBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    OffsetDateTime deletedAt
    
    // ❌ Eliminado: List<TaskFileDTO> files - Ya no se manejan archivos en tareas
) {}

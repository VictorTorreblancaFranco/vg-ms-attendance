package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record UpdateTaskRequestDTO(
    @NotNull(message = "Task ID is required")
    Long id,
    
    String title,
    String description,
    String instructions,
    Integer classId,
    Integer criterionId,
    Double pointsValue,
    OffsetDateTime dueDate,
    OffsetDateTime scheduledPublishDate,
    OffsetDateTime scheduledCloseDate
    
    // ❌ Eliminado: List<TaskFileDTO> files
) {}

package com.vg.task.domain.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TaskResponseDTO(
    Long id,
    Integer classId,
    Integer criterionId,
    String title,
    String description,
    String instructions,
    LocalDate assignmentDate,
    LocalDate dueDate,
    Double pointsValue,
    Short allowedAttempts,
    Boolean isGroupTask,
    Boolean visibleToParents,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}

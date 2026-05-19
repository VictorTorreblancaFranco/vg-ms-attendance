package com.vg.task.domain.dto;

public record RubricCriteriaResponseDTO(
    Long id,
    Long taskId,
    String name,
    String description,
    Double maxScore,
    Double weight,
    Integer sortOrder
) {}

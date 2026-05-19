package com.vg.task.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RubricCriteriaRequestDTO(
    @NotNull(message = "Task ID is required")
    Long taskId,
    
    @NotBlank(message = "Name is required")
    String name,
    
    String description,
    
    @NotNull(message = "Max score is required")
    @Min(value = 0, message = "Max score must be at least 0")
    Double maxScore,
    
    @NotNull(message = "Weight is required")
    @Min(value = 0, message = "Weight must be at least 0")
    Double weight,
    
    Integer sortOrder
) {}

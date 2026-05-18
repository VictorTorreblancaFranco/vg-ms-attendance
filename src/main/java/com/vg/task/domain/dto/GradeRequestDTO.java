package com.vg.task.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GradeRequestDTO(
    @NotNull(message = "Grade is required")
    @Min(value = 0, message = "Grade must be at least 0")
    @Max(value = 20, message = "Grade must be at most 20")
    Double grade,
    
    String feedback,
    
    @NotNull(message = "Graded by is required")
    Integer gradedBy
) {}

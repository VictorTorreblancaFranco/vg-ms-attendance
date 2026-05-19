package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotNull;

public record RubricScoreItemDTO(
    @NotNull(message = "Criterion ID is required")
    Long criterionId,
    
    @NotNull(message = "Score is required")
    Double score,
    
    String feedback
) {}

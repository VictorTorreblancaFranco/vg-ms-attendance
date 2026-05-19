package com.vg.task.domain.dto;

public record RubricScoreDTO(
    Long id,
    Long criterionId,
    Double score,
    String feedback
) {}

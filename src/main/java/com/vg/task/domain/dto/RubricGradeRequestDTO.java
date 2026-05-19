package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RubricGradeRequestDTO(
    @NotNull(message = "Rubric scores are required")
    List<RubricScoreItemDTO> scores,
    
    String privateComment,
    String publicComment,
    
    @NotNull(message = "Graded by is required")
    Integer gradedBy
) {}

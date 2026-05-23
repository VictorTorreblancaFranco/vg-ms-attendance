package com.vg.task.domain.dto;

public record TaskDifficultyDTO(
    Long taskId,
    String title,
    Double averageGrade,
    Long totalSubmissions,
    String difficulty
) {}

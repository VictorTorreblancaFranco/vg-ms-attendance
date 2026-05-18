package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotNull;

public record TaskSubmissionRequestDTO(
    @NotNull(message = "Task ID is required")
    Long taskId,
    
    @NotNull(message = "Student ID is required")
    Integer studentId,
    
    String studentComment
) {}

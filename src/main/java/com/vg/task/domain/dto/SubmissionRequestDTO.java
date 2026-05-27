package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotNull;

public record SubmissionRequestDTO(
    @NotNull(message = "Task ID is required")
    Long taskId,
    
    @NotNull(message = "Student ID is required")
    Integer studentId,
    
    String justificationReason,
    String privateComment,
    String publicComment
    
    // ❌ Eliminado: List<SubmissionFileDTO> files - Ya no se suben archivos
) {}

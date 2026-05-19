package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SubmissionRequestDTO(
    @NotNull(message = "Task ID is required")
    Long taskId,
    
    @NotNull(message = "Student ID is required")
    Integer studentId,
    
    String justificationReason,
    String privateComment,
    String publicComment,
    
    List<SubmissionFileDTO> files
) {}

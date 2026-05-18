package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public record TaskRequestDTO(
    @NotBlank(message = "Title is required")
    String title,
    
    String description,
    String instructions,
    
    @NotNull(message = "Class ID is required")
    Integer classId,
    
    Integer criterionId,
    
    Double pointsValue,
    
    @NotNull(message = "Due date is required")
    OffsetDateTime dueDate,
    
    @NotNull(message = "Created by is required")
    Integer createdBy,
    
    List<TaskFileDTO> files
) {}

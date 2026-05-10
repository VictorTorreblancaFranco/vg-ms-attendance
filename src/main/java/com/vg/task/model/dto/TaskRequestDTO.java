package com.vg.task.model.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record TaskRequestDTO(
    @NotNull(message = "Class ID is required")
    Integer classId,
    
    Integer criterionId,
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title max 255 characters")
    String title,
    
    String description,
    String instructions,
    
    @Future(message = "Due date must be in the future")
    LocalDate dueDate,
    
    @DecimalMin(value = "0.0", inclusive = true)
    Double pointsValue,
    
    @Min(value = 1, message = "At least 1 attempt allowed")
    Short allowedAttempts,
    
    Boolean isGroupTask,
    Boolean visibleToParents
) {}

package com.vg.task.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
    
    Double pointsValue,
    
    Short allowedAttempts,
    
    Boolean isGroupTask,
    
    Boolean visibleToParents
) {}

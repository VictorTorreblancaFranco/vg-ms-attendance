package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EducationalResourceDTO(
    Long id,
    @NotBlank(message = "Title is required") String title,
    String description,
    @NotBlank(message = "Type is required") String type,  // pdf, video, link, documento
    String url,
    String filePath,
    Integer subjectId,
    Integer gradeId,
    Integer createdBy,
    Boolean isPublic
) {}

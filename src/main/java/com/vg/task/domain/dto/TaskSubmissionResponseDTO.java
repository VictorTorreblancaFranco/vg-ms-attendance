package com.vg.task.domain.dto;

import java.time.OffsetDateTime;

public record TaskSubmissionResponseDTO(
    Long id,
    Long taskId,
    Integer studentId,
    Short attemptNumber,
    OffsetDateTime submissionDate,
    String status,
    Double grade,
    String feedback,
    Integer gradedBy,
    OffsetDateTime gradedAt,
    String studentComment,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}

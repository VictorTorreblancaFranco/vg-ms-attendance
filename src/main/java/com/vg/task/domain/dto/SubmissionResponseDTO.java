package com.vg.task.domain.dto;

import java.time.OffsetDateTime;

public record SubmissionResponseDTO(
    Long id,
    Long taskId,
    Integer studentId,
    OffsetDateTime submissionDate,
    String status,
    Double grade,
    String feedback,
    Integer gradedBy,
    OffsetDateTime gradedAt,
    String justificationReason,
    Boolean presented,
    OffsetDateTime presentedAt,
    String observations,
    Boolean isLate,
    OffsetDateTime justifiedAt,
    Integer justifiedBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}

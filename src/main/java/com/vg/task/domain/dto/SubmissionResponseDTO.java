package com.vg.task.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;

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
    String privateComment,
    String publicComment,
    Integer reattemptCount,
    Boolean reattemptAllowed,
    Integer maxReattempts,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<SubmissionFileDTO> files,
    List<CommentFileDTO> commentFiles,
    List<RubricScoreDTO> rubricScores
) {}

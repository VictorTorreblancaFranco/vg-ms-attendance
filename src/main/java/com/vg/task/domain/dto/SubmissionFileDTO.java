package com.vg.task.domain.dto;

public record SubmissionFileDTO(
    Long id,
    String fileName,
    String fileUrl,
    String fileType,
    Integer fileSizeKb
) {}

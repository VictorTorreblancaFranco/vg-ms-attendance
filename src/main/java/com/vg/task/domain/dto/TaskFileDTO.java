package com.vg.task.domain.dto;

public record TaskFileDTO(
    Long id,
    String fileName,
    String fileUrl,
    String fileType,
    Integer fileSizeKb
) {}

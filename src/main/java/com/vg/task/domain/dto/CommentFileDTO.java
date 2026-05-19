package com.vg.task.domain.dto;

public record CommentFileDTO(
    Long id,
    String fileName,
    String fileUrl,
    String fileType,
    Integer fileSizeKb
) {}

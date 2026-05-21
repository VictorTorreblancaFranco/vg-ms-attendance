package com.vg.task.domain.dto;

import java.util.List;

public record PageResponseDTO<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
    public static <T> PageResponseDTO<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        return new PageResponseDTO<>(
            content, pageNumber, pageSize, totalElements, totalPages,
            pageNumber == 0, pageNumber == totalPages - 1
        );
    }
}

package com.vg.task.domain.dto;

import java.time.OffsetDateTime;

public record TaskFilterDTO(
    String status,
    Integer classId,
    Integer createdBy,
    OffsetDateTime fromDate,
    OffsetDateTime toDate,
    Boolean isDeleted
) {}

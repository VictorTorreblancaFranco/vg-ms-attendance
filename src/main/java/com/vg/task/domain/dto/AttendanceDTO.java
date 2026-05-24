package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AttendanceDTO(
    Long id,
    @NotNull(message = "Class ID is required") Integer classId,
    @NotNull(message = "Student ID is required") Integer studentId,
    @NotNull(message = "Date is required") LocalDate date,
    @NotNull(message = "Status is required") String status,  // A, F, J
    String observation,
    Integer createdBy,
    String studentName,
    String studentCode
) {}

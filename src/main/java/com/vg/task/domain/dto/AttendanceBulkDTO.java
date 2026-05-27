package com.vg.task.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record AttendanceBulkDTO(
    @NotNull(message = "Class ID is required") 
    Integer classId,
    
    @NotNull(message = "Date is required") 
    LocalDate date,
    
    @NotNull(message = "Attendances list is required") 
    @Valid
    List<BulkAttendanceItem> attendances
) {
    public record BulkAttendanceItem(
        @NotNull(message = "Student ID is required") 
        Integer studentId,
        
        @NotNull(message = "Status is required") 
        String status,  // A, F, J, T
        
        String observation
    ) {}
}

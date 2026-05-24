package com.vg.task.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceBulkDTO {
    private Integer studentId;
    private Integer classId;
    private LocalDate date;
    private String status;  // A, F, J
    private String observation;
}

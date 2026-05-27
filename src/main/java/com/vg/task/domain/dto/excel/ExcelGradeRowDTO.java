package com.vg.task.domain.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelGradeRowDTO {
    private Integer studentId;
    private Long taskId;
    private Double grade;
    private String observations;
    private String justification;
    private Boolean isLate;
}

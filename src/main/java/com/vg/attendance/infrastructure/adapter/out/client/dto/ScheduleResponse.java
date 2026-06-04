package com.vg.attendance.infrastructure.adapter.out.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private Long id;
    private Long academicYearId;
    private Long gradeId;
    private Long sectionId;
    private Long courseId;
    private String teacherId;
    private Long classroomId;
    private Short dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean active;
}

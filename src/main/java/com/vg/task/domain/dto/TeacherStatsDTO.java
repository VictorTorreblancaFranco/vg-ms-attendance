package com.vg.task.domain.dto;

import java.util.Map;

public record TeacherStatsDTO(
    Long teacherId,
    String teacherName,
    Long totalTasks,
    Long totalSubmissions,
    Double averageGrade,
    Long pendingGrading,
    Long studentsWithLowGrades,
    Map<String, Long> tasksByStatus,
    Map<String, Double> averageBySubject
) {}

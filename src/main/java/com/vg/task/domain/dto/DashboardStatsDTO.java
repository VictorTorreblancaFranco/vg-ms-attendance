package com.vg.task.domain.dto;

import java.util.List;
import java.util.Map;

public record DashboardStatsDTO(
    Long totalTasks,
    Long totalSubmissions,
    Long totalStudentsSubmitted,
    Double averageGrade,
    Map<String, Long> tasksByStatus,
    Map<String, Long> submissionsByStatus,
    List<DailySubmissionDTO> dailySubmissions,
    GradeDistributionDTO gradeDistribution
) {}

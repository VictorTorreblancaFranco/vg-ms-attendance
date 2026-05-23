package com.vg.task.domain.dto;

import java.util.List;
import java.util.Map;

public record StudentSummaryDTO(
    Integer studentId,
    String studentName,
    Double overallAverage,
    Integer totalTasks,
    Integer totalPresented,
    Integer totalExcused,
    Map<String, Double> averageBySubject,
    List<SubmissionSummaryDTO> submissions
) {
    public record SubmissionSummaryDTO(
        Long taskId,
        String taskTitle,
        Double grade,
        Boolean presented,
        String status,
        String observations
    ) {}
}

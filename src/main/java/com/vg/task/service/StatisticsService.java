package com.vg.task.service;

import com.vg.task.domain.dto.TaskDifficultyDTO;
import com.vg.task.domain.dto.TeacherStatsDTO;
import com.vg.task.domain.dto.StudentSummaryDTO;
import com.vg.task.domain.model.Task;
import reactor.core.publisher.Mono;
import java.util.List;

public interface StatisticsService {
    Mono<List<TaskDifficultyDTO>> getHardestTasks(int limit);
    Mono<TeacherStatsDTO> getTeacherStats(Integer teacherId);
    Mono<StudentSummaryDTO> getStudentSummary(Integer studentId);
    Mono<List<Task>> getPendingGradingTasks();
}

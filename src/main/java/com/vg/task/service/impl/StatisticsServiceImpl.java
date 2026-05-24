package com.vg.task.service.impl;

import com.vg.task.domain.dto.TaskDifficultyDTO;
import com.vg.task.domain.dto.TeacherStatsDTO;
import com.vg.task.domain.dto.StudentSummaryDTO;
import com.vg.task.domain.model.Submission;
import com.vg.task.domain.model.Task;
import com.vg.task.repository.SubmissionRepository;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public Mono<List<TaskDifficultyDTO>> getHardestTasks(int limit) {
        return submissionRepository.findAll()
            .filter(s -> s.getGrade() != null)
            .collectList()
            .flatMap(submissions -> {
                Map<Long, Double> sumMap = new HashMap<>();
                Map<Long, Long> countMap = new HashMap<>();
                for (Submission s : submissions) {
                    sumMap.merge(s.getTaskId(), s.getGrade(), Double::sum);
                    countMap.merge(s.getTaskId(), 1L, Long::sum);
                }
                return taskRepository.findAll()
                    .collectList()
                    .map(tasks -> {
                        List<TaskDifficultyDTO> result = new ArrayList<>();
                        for (Map.Entry<Long, Double> e : sumMap.entrySet()) {
                            Long taskId = e.getKey();
                            double avg = e.getValue() / countMap.get(taskId);
                            Task task = tasks.stream().filter(t -> t.getId().equals(taskId)).findFirst().orElse(null);
                            String difficulty = avg >= 16 ? "Fácil" : (avg >= 11 ? "Media" : "Difícil");
                            result.add(new TaskDifficultyDTO(taskId, task != null ? task.getTitle() : "Tarea " + taskId, avg, countMap.get(taskId), difficulty));
                        }
                        result.sort(Comparator.comparingDouble(TaskDifficultyDTO::averageGrade));
                        return result.stream().limit(limit).collect(Collectors.toList());
                    });
            });
    }

    @Override
    public Mono<TeacherStatsDTO> getTeacherStats(Integer teacherId) {
        return taskRepository.findByCreatedByAndIsDeletedFalse(teacherId)
            .collectList()
            .flatMap(tasks -> {
                if (tasks.isEmpty()) {
                    return Mono.just(new TeacherStatsDTO(Long.valueOf(teacherId), "", 0L, 0L, 0.0, 0L, 0L, new HashMap<>(), new HashMap<>()));
                }
                List<Long> taskIds = tasks.stream().map(Task::getId).collect(Collectors.toList());
                return submissionRepository.findAll()
                    .filter(s -> taskIds.contains(s.getTaskId()))
                    .collectList()
                    .map(submissions -> {
                        double avgGrade = submissions.stream().filter(s -> s.getGrade() != null).mapToDouble(Submission::getGrade).average().orElse(0.0);
                        long pendingGrading = submissions.stream().filter(s -> !"graded".equals(s.getStatus())).count();
                        long lowGradeStudents = submissions.stream().filter(s -> s.getGrade() != null && s.getGrade() < 11).map(Submission::getStudentId).distinct().count();
                        return new TeacherStatsDTO(Long.valueOf(teacherId), "", (long) tasks.size(), (long) submissions.size(), avgGrade, pendingGrading, lowGradeStudents, new HashMap<>(), new HashMap<>());
                    });
            });
    }

    @Override
    public Mono<StudentSummaryDTO> getStudentSummary(Integer studentId) {
        return submissionRepository.findByStudentId(studentId)
            .collectList()
            .flatMap(submissions -> {
                if (submissions.isEmpty()) {
                    return Mono.just(new StudentSummaryDTO(studentId, "", 0.0, 0, 0, 0, new HashMap<>(), new ArrayList<>()));
                }
                return taskRepository.findAll()
                    .collectList()
                    .map(tasks -> {
                        double overallAvg = submissions.stream().filter(s -> s.getGrade() != null).mapToDouble(Submission::getGrade).average().orElse(0.0);
                        int totalPresented = (int) submissions.stream().filter(s -> s.getPresented() != null && s.getPresented()).count();
                        int totalExcused = (int) submissions.stream().filter(s -> "excused".equals(s.getStatus())).count();
                        return new StudentSummaryDTO(studentId, "", overallAvg, submissions.size(), totalPresented, totalExcused, new HashMap<>(), new ArrayList<>());
                    });
            });
    }

    @Override
    public Mono<List<Task>> getPendingGradingTasks() {
        return submissionRepository.findAll()
            .filter(s -> !"graded".equals(s.getStatus()))
            .map(Submission::getTaskId)
            .distinct()
            .flatMap(taskId -> taskRepository.findById(taskId))
            .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
            .collectList();
    }
}

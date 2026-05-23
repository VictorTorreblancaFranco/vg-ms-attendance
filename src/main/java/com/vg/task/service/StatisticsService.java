package com.vg.task.service;

import com.vg.task.domain.dto.TaskDifficultyDTO;
import com.vg.task.domain.dto.TeacherStatsDTO;
import com.vg.task.domain.dto.StudentSummaryDTO;
import com.vg.task.domain.model.Submission;
import com.vg.task.domain.model.Task;
import com.vg.task.repository.SubmissionRepository;
import com.vg.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;

    public Mono<List<TaskDifficultyDTO>> getHardestTasks(int limit) {
        return submissionRepository.findAll()
            .filter(s -> s.getGrade() != null)
            .collectList()
            .flatMap(submissions -> {
                Map<Long, Double> avgMap = new HashMap<>();
                Map<Long, Long> countMap = new HashMap<>();
                
                for (Submission s : submissions) {
                    avgMap.merge(s.getTaskId(), s.getGrade(), Double::sum);
                    countMap.merge(s.getTaskId(), 1L, Long::sum);
                }
                
                return taskRepository.findAll()
                    .collectList()
                    .map(tasks -> {
                        List<TaskDifficultyDTO> result = avgMap.entrySet().stream()
                            .map(e -> {
                                Long taskId = e.getKey();
                                double avg = e.getValue() / countMap.get(taskId);
                                Task task = tasks.stream().filter(t -> t.getId().equals(taskId)).findFirst().orElse(null);
                                String difficulty = avg >= 16 ? "Fácil" : (avg >= 11 ? "Media" : "Difícil");
                                return new TaskDifficultyDTO(
                                    taskId,
                                    task != null ? task.getTitle() : "Tarea " + taskId,
                                    avg,
                                    countMap.get(taskId),
                                    difficulty
                                );
                            })
                            .sorted(Comparator.comparingDouble(TaskDifficultyDTO::averageGrade))
                            .limit(limit)
                            .collect(Collectors.toList());
                        return result;
                    });
            });
    }

    public Mono<TeacherStatsDTO> getTeacherStats(Integer teacherId) {
        return taskRepository.findByCreatedByAndIsDeletedFalse(teacherId)
            .collectList()
            .flatMap(tasks -> {
                if (tasks.isEmpty()) {
                    return Mono.just(new TeacherStatsDTO(
                        Long.valueOf(teacherId), "", 0L, 0L, 0.0, 0L, 0L,
                        new HashMap<>(), new HashMap<>()
                    ));
                }
                
                List<Long> taskIds = tasks.stream().map(Task::getId).collect(Collectors.toList());
                return submissionRepository.findAll()
                    .filter(s -> taskIds.contains(s.getTaskId()))
                    .collectList()
                    .map(submissions -> {
                        long totalTasks = tasks.size();
                        long totalSubmissions = submissions.size();
                        double avgGrade = submissions.stream()
                            .filter(s -> s.getGrade() != null)
                            .mapToDouble(Submission::getGrade)
                            .average()
                            .orElse(0.0);
                        long pendingGrading = submissions.stream()
                            .filter(s -> !"graded".equals(s.getStatus()))
                            .count();
                        long lowGradeStudents = submissions.stream()
                            .filter(s -> s.getGrade() != null && s.getGrade() < 11)
                            .map(Submission::getStudentId)
                            .distinct()
                            .count();
                        
                        Map<String, Long> tasksByStatus = new HashMap<>();
                        tasksByStatus.put("draft", tasks.stream().filter(t -> "draft".equals(t.getStatus())).count());
                        tasksByStatus.put("published", tasks.stream().filter(t -> "published".equals(t.getStatus())).count());
                        tasksByStatus.put("closed", tasks.stream().filter(t -> "closed".equals(t.getStatus())).count());
                        
                        return new TeacherStatsDTO(
                            Long.valueOf(teacherId), "", totalTasks, totalSubmissions,
                            avgGrade, pendingGrading, lowGradeStudents,
                            tasksByStatus, new HashMap<>()
                        );
                    });
            });
    }

    public Mono<StudentSummaryDTO> getStudentSummary(Integer studentId) {
        return submissionRepository.findByStudentId(studentId)
            .collectList()
            .flatMap(submissions -> {
                if (submissions.isEmpty()) {
                    return Mono.just(new StudentSummaryDTO(
                        studentId, "", 0.0, 0, 0, 0,
                        new HashMap<>(), List.of()
                    ));
                }
                
                List<Long> taskIds = submissions.stream().map(Submission::getTaskId).collect(Collectors.toList());
                return taskRepository.findAll()
                    .filter(t -> taskIds.contains(t.getId()))
                    .collectList()
                    .map(tasks -> {
                        double overallAvg = submissions.stream()
                            .filter(s -> s.getGrade() != null)
                            .mapToDouble(Submission::getGrade)
                            .average()
                            .orElse(0.0);
                        int totalTasks = submissions.size();
                        int totalPresented = (int) submissions.stream().filter(s -> s.getPresented() != null && s.getPresented()).count();
                        int totalExcused = (int) submissions.stream().filter(s -> "excused".equals(s.getStatus())).count();
                        
                        List<StudentSummaryDTO.SubmissionSummaryDTO> submissionSummaries = submissions.stream()
                            .map(s -> {
                                Task task = tasks.stream().filter(t -> t.getId().equals(s.getTaskId())).findFirst().orElse(null);
                                return new StudentSummaryDTO.SubmissionSummaryDTO(
                                    s.getTaskId(),
                                    task != null ? task.getTitle() : "N/A",
                                    s.getGrade(),
                                    s.getPresented(),
                                    s.getStatus(),
                                    s.getObservations()
                                );
                            })
                            .collect(Collectors.toList());
                        
                        return new StudentSummaryDTO(
                            studentId, "", overallAvg, totalTasks, totalPresented, totalExcused,
                            new HashMap<>(), submissionSummaries
                        );
                    });
            });
    }

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

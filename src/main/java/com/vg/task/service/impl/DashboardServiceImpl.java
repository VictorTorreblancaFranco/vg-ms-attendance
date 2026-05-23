package com.vg.task.service.impl;

import com.vg.task.client.StudentClient;
import com.vg.task.domain.dto.DashboardStatsDTO;
import com.vg.task.domain.dto.DailySubmissionDTO;
import com.vg.task.domain.dto.GradeDistributionDTO;
import com.vg.task.repository.SubmissionRepository;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentClient studentClient;

    @Override
    public Mono<DashboardStatsDTO> getDashboardStats() {
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        
        return Mono.zip(
            getTotalTasks(),
            getTotalSubmissions(),
            getTotalStudentsSubmitted(),
            getAverageGrade(),
            getTasksByStatus(),
            getSubmissionsByStatus(),
            getDailySubmissions(thirtyDaysAgo),
            getGradeDistribution()
        ).map(tuple -> new DashboardStatsDTO(
            tuple.getT1(),    // totalTasks
            tuple.getT2(),    // totalSubmissions
            tuple.getT3(),    // totalStudentsSubmitted
            tuple.getT4(),    // averageGrade
            tuple.getT5(),    // tasksByStatus
            tuple.getT6(),    // submissionsByStatus
            tuple.getT7(),    // dailySubmissions
            tuple.getT8()     // gradeDistribution
        ));
    }

    @Override
    public Mono<DashboardStatsDTO> getTaskStats(Long taskId) {
        return submissionRepository.findByTaskId(taskId)
            .collectList()
            .map(submissions -> {
                long count = submissions.size();
                double avg = submissions.stream()
                        .filter(s -> s.getGrade() != null)
                        .mapToDouble(s -> s.getGrade())
                        .average()
                        .orElse(0.0);
                
                Map<String, Long> byStatus = new HashMap<>();
                byStatus.put("submitted", submissions.stream().filter(s -> "submitted".equals(s.getStatus())).count());
                byStatus.put("graded", submissions.stream().filter(s -> "graded".equals(s.getStatus())).count());
                byStatus.put("late", submissions.stream().filter(s -> "late".equals(s.getStatus())).count());
                
                GradeDistributionDTO gradeDist = getDistributionFromSubmissions(submissions);
                
                return new DashboardStatsDTO(
                    1L,                           // totalTasks
                    (long) submissions.size(),    // totalSubmissions
                    (long) submissions.stream().map(s -> s.getStudentId()).distinct().count(), // totalStudentsSubmitted
                    avg,                          // averageGrade
                    null,                         // tasksByStatus
                    byStatus,                     // submissionsByStatus
                    null,                         // dailySubmissions
                    gradeDist                     // gradeDistribution
                );
            });
    }

    private Mono<Long> getTotalTasks() {
        return taskRepository.count();
    }

    private Mono<Long> getTotalSubmissions() {
        return submissionRepository.count();
    }

    private Mono<Long> getTotalStudentsSubmitted() {
        return submissionRepository.findAll()
                .map(s -> s.getStudentId())
                .distinct()
                .count();
    }

    private Mono<Double> getAverageGrade() {
        return submissionRepository.findAll()
                .filter(s -> s.getGrade() != null)
                .map(s -> s.getGrade())
                .collectList()
                .map(grades -> grades.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    private Mono<Map<String, Long>> getTasksByStatus() {
        return taskRepository.findAll()
                .filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()))
                .collectList()
                .map(tasks -> {
                    Map<String, Long> map = new HashMap<>();
                    map.put("draft", tasks.stream().filter(t -> "draft".equals(t.getStatus())).count());
                    map.put("published", tasks.stream().filter(t -> "published".equals(t.getStatus())).count());
                    map.put("closed", tasks.stream().filter(t -> "closed".equals(t.getStatus())).count());
                    map.put("archived", tasks.stream().filter(t -> "archived".equals(t.getStatus())).count());
                    return map;
                });
    }

    private Mono<Map<String, Long>> getSubmissionsByStatus() {
        return submissionRepository.findAll()
                .collectList()
                .map(submissions -> {
                    Map<String, Long> map = new HashMap<>();
                    map.put("submitted", submissions.stream().filter(s -> "submitted".equals(s.getStatus())).count());
                    map.put("graded", submissions.stream().filter(s -> "graded".equals(s.getStatus())).count());
                    map.put("late", submissions.stream().filter(s -> "late".equals(s.getStatus())).count());
                    map.put("excused", submissions.stream().filter(s -> "excused".equals(s.getStatus())).count());
                    return map;
                });
    }

    private Mono<List<DailySubmissionDTO>> getDailySubmissions(OffsetDateTime fromDate) {
        return submissionRepository.findAll()
                .filter(s -> s.getSubmissionDate().isAfter(fromDate))
                .collectList()
                .map(submissions -> {
                    Map<String, Long> daily = new HashMap<>();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    for (var sub : submissions) {
                        String date = sub.getSubmissionDate().format(formatter);
                        daily.put(date, daily.getOrDefault(date, 0L) + 1);
                    }
                    return daily.entrySet().stream()
                            .map(e -> new DailySubmissionDTO(e.getKey(), e.getValue()))
                            .collect(Collectors.toList());
                });
    }

    private Mono<GradeDistributionDTO> getGradeDistribution() {
        return submissionRepository.findAll()
                .filter(s -> s.getGrade() != null)
                .map(s -> s.getGrade())
                .collectList()
                .map(this::getDistributionFromGrades);
    }

    private GradeDistributionDTO getDistributionFromGrades(List<Double> grades) {
        long c0_5 = grades.stream().filter(g -> g >= 0 && g <= 5).count();
        long c6_10 = grades.stream().filter(g -> g >= 6 && g <= 10).count();
        long c11_15 = grades.stream().filter(g -> g >= 11 && g <= 15).count();
        long c16_20 = grades.stream().filter(g -> g >= 16 && g <= 20).count();
        return new GradeDistributionDTO(c0_5, c6_10, c11_15, c16_20);
    }

    private GradeDistributionDTO getDistributionFromSubmissions(List<com.vg.task.domain.model.Submission> submissions) {
        long c0_5 = submissions.stream().filter(s -> s.getGrade() != null && s.getGrade() >= 0 && s.getGrade() <= 5).count();
        long c6_10 = submissions.stream().filter(s -> s.getGrade() != null && s.getGrade() >= 6 && s.getGrade() <= 10).count();
        long c11_15 = submissions.stream().filter(s -> s.getGrade() != null && s.getGrade() >= 11 && s.getGrade() <= 15).count();
        long c16_20 = submissions.stream().filter(s -> s.getGrade() != null && s.getGrade() >= 16 && s.getGrade() <= 20).count();
        return new GradeDistributionDTO(c0_5, c6_10, c11_15, c16_20);
    }
}

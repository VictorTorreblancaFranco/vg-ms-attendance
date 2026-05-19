package com.vg.task.service.impl;

import com.vg.task.domain.dto.DashboardStatsDTO;
import com.vg.task.domain.dto.DailySubmissionDTO;
import com.vg.task.domain.dto.GradeDistributionDTO;
import com.vg.task.repository.SubmissionRepository;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public Mono<DashboardStatsDTO> getDashboardStats() {
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        
        return Mono.zip(
            taskRepository.count(),
            submissionRepository.count(),
            getTotalStudentsSubmitted(),
            getAverageGrade(),
            getTasksByStatus(),
            getSubmissionsByStatus(),
            getDailySubmissions(thirtyDaysAgo),
            getGradeDistribution()
        ).map(tuple -> new DashboardStatsDTO(
            tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(),
            tuple.getT5(), tuple.getT6(), tuple.getT7(), tuple.getT8()
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
                
                return new DashboardStatsDTO(1L, (long) submissions.size(), count, avg,
                        null, byStatus, null, getDistributionFromSubmissions(submissions));
            });
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
                .map(t -> t.getStatus())
                .collectList()
                .map(statuses -> {
                    Map<String, Long> map = new HashMap<>();
                    map.put("draft", statuses.stream().filter(s -> "draft".equals(s)).count());
                    map.put("published", statuses.stream().filter(s -> "published".equals(s)).count());
                    map.put("closed", statuses.stream().filter(s -> "closed".equals(s)).count());
                    map.put("archived", statuses.stream().filter(s -> "archived".equals(s)).count());
                    return map;
                });
    }

    private Mono<Map<String, Long>> getSubmissionsByStatus() {
        return submissionRepository.findAll()
                .map(s -> s.getStatus())
                .collectList()
                .map(statuses -> {
                    Map<String, Long> map = new HashMap<>();
                    map.put("submitted", statuses.stream().filter(s -> "submitted".equals(s)).count());
                    map.put("graded", statuses.stream().filter(s -> "graded".equals(s)).count());
                    map.put("late", statuses.stream().filter(s -> "late".equals(s)).count());
                    map.put("excused", statuses.stream().filter(s -> "excused".equals(s)).count());
                    return map;
                });
    }

    private Mono<GradeDistributionDTO> getGradeDistribution() {
        return submissionRepository.findAll()
                .filter(s -> s.getGrade() != null)
                .map(s -> s.getGrade())
                .collectList()
                .map(this::getDistributionFromGrades);
    }

    private Mono<java.util.List<DailySubmissionDTO>> getDailySubmissions(OffsetDateTime fromDate) {
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
                            .toList();
                });
    }

    private GradeDistributionDTO getDistributionFromGrades(java.util.List<Double> grades) {
        long c0_5 = grades.stream().filter(g -> g >= 0 && g <= 5).count();
        long c6_10 = grades.stream().filter(g -> g >= 6 && g <= 10).count();
        long c11_15 = grades.stream().filter(g -> g >= 11 && g <= 15).count();
        long c16_20 = grades.stream().filter(g -> g >= 16 && g <= 20).count();
        return new GradeDistributionDTO(c0_5, c6_10, c11_15, c16_20);
    }

    private GradeDistributionDTO getDistributionFromSubmissions(java.util.List<com.vg.task.domain.model.Submission> submissions) {
        long c0_5 = submissions.stream().filter(s -> s.getGrade() != null && s.getGrade() >= 0 && s.getGrade() <= 5).count();
        long c6_10 = submissions.stream().filter(s -> s.getGrade() != null && s.getGrade() >= 6 && s.getGrade() <= 10).count();
        long c11_15 = submissions.stream().filter(s -> s.getGrade() != null && s.getGrade() >= 11 && s.getGrade() <= 15).count();
        long c16_20 = submissions.stream().filter(s -> s.getGrade() != null && s.getGrade() >= 16 && s.getGrade() <= 20).count();
        return new GradeDistributionDTO(c0_5, c6_10, c11_15, c16_20);
    }
}

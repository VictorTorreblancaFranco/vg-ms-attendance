package com.vg.task.web.controller;

import com.vg.task.domain.dto.TaskDifficultyDTO;
import com.vg.task.domain.dto.TeacherStatsDTO;
import com.vg.task.domain.dto.StudentSummaryDTO;
import com.vg.task.domain.model.Task;
import com.vg.task.service.StatisticsService;
import com.vg.task.service.impl.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final PdfExportService pdfExportService;

    @GetMapping("/hardest-tasks")
    public Mono<List<TaskDifficultyDTO>> getHardestTasks(@RequestParam(defaultValue = "5") int limit) {
        return statisticsService.getHardestTasks(limit);
    }

    @GetMapping("/teacher/{teacherId}")
    public Mono<TeacherStatsDTO> getTeacherStats(@PathVariable Integer teacherId) {
        return statisticsService.getTeacherStats(teacherId);
    }

    @GetMapping("/student/{studentId}")
    public Mono<StudentSummaryDTO> getStudentSummary(@PathVariable Integer studentId) {
        return statisticsService.getStudentSummary(studentId);
    }

    @GetMapping("/pending-grading")
    public Mono<List<Task>> getPendingGrading() {
        return statisticsService.getPendingGradingTasks();
    }

    @GetMapping(value = "/transcript/{studentId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<byte[]> exportTranscript(@PathVariable Integer studentId, @RequestParam(defaultValue = "Estudiante") String name) {
        return pdfExportService.exportTranscript(studentId, name);
    }
}

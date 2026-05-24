package com.vg.task.web.handler;

import com.vg.task.domain.dto.TaskDifficultyDTO;
import com.vg.task.domain.dto.TeacherStatsDTO;
import com.vg.task.domain.dto.StudentSummaryDTO;
import com.vg.task.domain.model.Task;
import com.vg.task.service.impl.StatisticsServiceImpl;
import com.vg.task.service.impl.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class StatisticsHandler {

    private final StatisticsServiceImpl statisticsService;
    private final PdfExportService pdfExportService;

    public Mono<ServerResponse> getHardestTasks(ServerRequest request) {
        int limit = Integer.parseInt(request.queryParam("limit").orElse("5"));
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(statisticsService.getHardestTasks(limit), TaskDifficultyDTO.class);
    }

    public Mono<ServerResponse> getTeacherStats(ServerRequest request) {
        Integer teacherId = Integer.parseInt(request.pathVariable("teacherId"));
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(statisticsService.getTeacherStats(teacherId), TeacherStatsDTO.class);
    }

    public Mono<ServerResponse> getStudentSummary(ServerRequest request) {
        Integer studentId = Integer.parseInt(request.pathVariable("studentId"));
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(statisticsService.getStudentSummary(studentId), StudentSummaryDTO.class);
    }

    public Mono<ServerResponse> getPendingGrading(ServerRequest request) {
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(statisticsService.getPendingGradingTasks(), Task.class);
    }

    public Mono<ServerResponse> exportTranscript(ServerRequest request) {
        Integer studentId = Integer.parseInt(request.pathVariable("studentId"));
        String studentName = request.queryParam("name").orElse("Estudiante");
        return pdfExportService.exportTranscript(studentId, studentName)
            .flatMap(data -> ServerResponse.ok()
                .header("Content-Disposition", "attachment; filename=transcript_" + studentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .bodyValue(data));
    }
}

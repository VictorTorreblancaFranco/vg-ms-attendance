package com.vg.task.web.handler;

import com.vg.task.domain.dto.*;
import com.vg.task.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SubmissionHandler {

    private final SubmissionService submissionService;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(submissionService.findAll(), SubmissionResponseDTO.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return submissionService.findById(id)
                .flatMap(submission -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submission))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByTaskId(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(submissionService.findByTaskId(taskId), SubmissionResponseDTO.class);
    }

    public Mono<ServerResponse> findByStudentId(ServerRequest request) {
        Integer studentId = Integer.parseInt(request.pathVariable("studentId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(submissionService.findByStudentId(studentId), SubmissionResponseDTO.class);
    }

    public Mono<ServerResponse> submit(ServerRequest request) {
        return request.bodyToMono(SubmissionRequestDTO.class)
                .flatMap(submissionService::submit)
                .flatMap(submission -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submission))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }

    public Mono<ServerResponse> grade(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(GradeRequestDTO.class)
                .flatMap(dto -> submissionService.grade(id, dto))
                .flatMap(submission -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submission));
    }

    public Mono<ServerResponse> gradeWithRubric(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(RubricGradeRequestDTO.class)
                .flatMap(dto -> submissionService.gradeWithRubric(id, dto))
                .flatMap(submission -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submission));
    }

    public Mono<ServerResponse> allowReattempt(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        String maxParam = request.queryParam("max").orElse("1");
        Integer maxAttempts = Integer.parseInt(maxParam);
        return submissionService.allowReattempt(id, maxAttempts)
                .flatMap(submission -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submission));
    }

    public Mono<ServerResponse> excuse(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        String reason = request.queryParam("reason").orElse("No reason provided");
        return submissionService.excuse(id, reason)
                .flatMap(submission -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submission));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return submissionService.delete(id)
                .then(ServerResponse.noContent().build());
    }
}

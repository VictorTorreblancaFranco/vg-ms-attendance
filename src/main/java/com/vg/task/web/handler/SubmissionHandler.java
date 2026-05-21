package com.vg.task.web.handler;

import com.vg.task.application.port.input.SubmissionUseCase;
import com.vg.task.domain.dto.*;
import com.vg.task.mapper.SubmissionMapper;
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

    private final SubmissionUseCase submissionUseCase;
    private final SubmissionMapper mapper;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(submissionUseCase.findAll().map(mapper::toResponse), SubmissionResponseDTO.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return submissionUseCase.findById(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByTaskId(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(submissionUseCase.findByTaskId(taskId).map(mapper::toResponse), SubmissionResponseDTO.class);
    }

    public Mono<ServerResponse> findByStudentId(ServerRequest request) {
        Integer studentId = Integer.parseInt(request.pathVariable("studentId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(submissionUseCase.findByStudentId(studentId).map(mapper::toResponse), SubmissionResponseDTO.class);
    }

    public Mono<ServerResponse> submit(ServerRequest request) {
        return request.bodyToMono(SubmissionRequestDTO.class)
                .map(mapper::toDomain)
                .flatMap(submissionUseCase::submit)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }

    public Mono<ServerResponse> grade(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(GradeRequestDTO.class)
                .flatMap(dto -> submissionUseCase.grade(id, dto))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> gradeWithRubric(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(RubricGradeRequestDTO.class)
                .flatMap(dto -> submissionUseCase.gradeWithRubric(id, dto))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> allowReattempt(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        String maxParam = request.queryParam("max").orElse("1");
        Integer maxAttempts = Integer.parseInt(maxParam);
        return submissionUseCase.allowReattempt(id, maxAttempts)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> excuse(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        String reason = request.queryParam("reason").orElse("No reason provided");
        return submissionUseCase.excuse(id, reason)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return submissionUseCase.delete(id)
                .then(ServerResponse.noContent().build());
    }
}

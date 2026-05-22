package com.vg.task.web.handler;

import com.vg.task.application.port.input.SubmissionUseCase;
import com.vg.task.domain.dto.*;
import com.vg.task.mapper.SubmissionMapper;
import com.vg.task.service.RateLimitService;
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
    private final RateLimitService rateLimitService;

    private Mono<ServerResponse> checkRateLimit(ServerRequest request) {
        String clientIp = request.remoteAddress()
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        String path = request.path();

        return rateLimitService.allowRequest(clientIp, path)
                .flatMap(allowed -> {
                    if (!allowed) {
                        return ServerResponse.status(429)
                                .bodyValue(Map.of(
                                        "error", "Too Many Requests",
                                        "message", "Has excedido el límite de peticiones para este endpoint",
                                        "status", 429
                                ));
                    }
                    return Mono.empty();
                });
    }

    private Mono<ServerResponse> withRateLimit(ServerRequest request, Mono<ServerResponse> response) {
        return checkRateLimit(request).switchIfEmpty(response);
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return withRateLimit(request, ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(submissionUseCase.findAll().map(mapper::toResponse), SubmissionResponseDTO.class));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, submissionUseCase.findById(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build()));
    }

    public Mono<ServerResponse> findByTaskId(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        return withRateLimit(request, ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(submissionUseCase.findByTaskId(taskId).map(mapper::toResponse), SubmissionResponseDTO.class));
    }

    public Mono<ServerResponse> findByStudentId(ServerRequest request) {
        Integer studentId = Integer.parseInt(request.pathVariable("studentId"));
        return withRateLimit(request, ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(submissionUseCase.findByStudentId(studentId).map(mapper::toResponse), SubmissionResponseDTO.class));
    }

    public Mono<ServerResponse> submit(ServerRequest request) {
        return withRateLimit(request, request.bodyToMono(SubmissionRequestDTO.class)
                .map(mapper::toDomain)
                .flatMap(submissionUseCase::submit)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage()))));
    }

    public Mono<ServerResponse> grade(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, request.bodyToMono(GradeRequestDTO.class)
                .flatMap(dto -> submissionUseCase.grade(id, dto))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response)));
    }

    public Mono<ServerResponse> gradeWithRubric(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, request.bodyToMono(RubricGradeRequestDTO.class)
                .flatMap(dto -> submissionUseCase.gradeWithRubric(id, dto))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response)));
    }

    public Mono<ServerResponse> allowReattempt(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        String maxParam = request.queryParam("max").orElse("1");
        Integer maxAttempts = Integer.parseInt(maxParam);
        return withRateLimit(request, submissionUseCase.allowReattempt(id, maxAttempts)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response)));
    }

    public Mono<ServerResponse> excuse(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        String reason = request.queryParam("reason").orElse("No reason provided");
        return withRateLimit(request, submissionUseCase.excuse(id, reason)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response)));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, submissionUseCase.delete(id)
                .then(ServerResponse.noContent().build()));
    }
}

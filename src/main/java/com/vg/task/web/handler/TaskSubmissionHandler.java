package com.vg.task.web.handler;

import com.vg.task.domain.dto.GradeSubmissionRequestDTO;
import com.vg.task.domain.dto.TaskSubmissionRequestDTO;
import com.vg.task.domain.dto.TaskSubmissionResponseDTO;
import com.vg.task.service.TaskSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TaskSubmissionHandler {

    private final TaskSubmissionService taskSubmissionService;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskSubmissionService.findAll(), TaskSubmissionResponseDTO.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskSubmissionService.findById(id)
                .flatMap(submission -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submission))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> submit(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        Integer userId = getUserIdFromRequest(request);
        
        return request.bodyToMono(TaskSubmissionRequestDTO.class)
                .flatMap(dto -> taskSubmissionService.submit(
                    new TaskSubmissionRequestDTO(taskId, dto.studentId(), dto.studentComment()),
                    userId
                ))
                .flatMap(submission -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submission));
    }

    public Mono<ServerResponse> grade(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(GradeSubmissionRequestDTO.class)
                .flatMap(dto -> taskSubmissionService.grade(id, dto))
                .flatMap(submission -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submission));
    }

    public Mono<ServerResponse> findByTaskId(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskSubmissionService.findByTaskId(taskId), TaskSubmissionResponseDTO.class);
    }

    public Mono<ServerResponse> findByStudentId(ServerRequest request) {
        Integer studentId = Integer.parseInt(request.pathVariable("studentId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskSubmissionService.findByStudentId(studentId), TaskSubmissionResponseDTO.class);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskSubmissionService.delete(id)
                .then(ServerResponse.noContent().build());
    }

    private Integer getUserIdFromRequest(ServerRequest request) {
        return request.headers().firstHeader("X-User-Id") != null 
            ? Integer.parseInt(request.headers().firstHeader("X-User-Id")) 
            : 1; // Default for development
    }
}

package com.vg.task.web.handler;

import com.vg.task.domain.dto.RubricCriteriaRequestDTO;
import com.vg.task.domain.dto.RubricCriteriaResponseDTO;
import com.vg.task.service.RubricCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RubricCriteriaHandler {

    private final RubricCriteriaService rubricCriteriaService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(RubricCriteriaRequestDTO.class)
                .flatMap(rubricCriteriaService::create)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> findByTaskId(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(rubricCriteriaService.findByTaskId(taskId), RubricCriteriaResponseDTO.class);
    }
}

package com.vg.task.web.handler;

import com.vg.task.application.port.input.TaskUseCase;
import com.vg.task.domain.dto.TaskFilterDTO;
import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.domain.dto.TaskResponseDTO;
import com.vg.task.domain.dto.UpdateTaskRequestDTO;
import com.vg.task.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaskHandler {

    private final TaskUseCase taskUseCase;
    private final TaskMapper mapper;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskUseCase.findAll().map(mapper::toResponse), TaskResponseDTO.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskUseCase.findById(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByStatus(ServerRequest request) {
        String status = request.pathVariable("status");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskUseCase.findByStatus(status).map(mapper::toResponse), TaskResponseDTO.class);
    }

    public Mono<ServerResponse> findByClassId(ServerRequest request) {
        Integer classId = Integer.parseInt(request.pathVariable("classId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskUseCase.findByClassId(classId).map(mapper::toResponse), TaskResponseDTO.class);
    }

    public Mono<ServerResponse> filter(ServerRequest request) {
        TaskFilterDTO filter = new TaskFilterDTO(
                request.queryParam("status").orElse(null),
                request.queryParam("classId").map(Integer::parseInt).orElse(null),
                request.queryParam("createdBy").map(Integer::parseInt).orElse(null),
                request.queryParam("fromDate").map(OffsetDateTime::parse).orElse(null),
                request.queryParam("toDate").map(OffsetDateTime::parse).orElse(null),
                false
        );
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskUseCase.filter(filter).map(mapper::toResponse), TaskResponseDTO.class);
    }

    public Mono<ServerResponse> exportCsv(ServerRequest request) {
        TaskFilterDTO filter = new TaskFilterDTO(null, null, null, null, null, false);
        return taskUseCase.exportToCsv(filter)
                .flatMap(data -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=tasks.csv")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .bodyValue(data));
    }

    public Mono<ServerResponse> exportExcel(ServerRequest request) {
        TaskFilterDTO filter = new TaskFilterDTO(null, null, null, null, null, false);
        return taskUseCase.exportToExcel(filter)
                .flatMap(data -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=tasks.xlsx")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .bodyValue(data));
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(TaskRequestDTO.class)
                .map(mapper::toDomain)
                .flatMap(taskUseCase::save)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        return request.bodyToMono(UpdateTaskRequestDTO.class)
                .flatMap(dto -> taskUseCase.findById(dto.id())
                        .flatMap(existing -> {
                            if (dto.title() != null) existing.setTitle(dto.title());
                            if (dto.description() != null) existing.setDescription(dto.description());
                            if (dto.instructions() != null) existing.setInstructions(dto.instructions());
                            if (dto.classId() != null) existing.setClassId(dto.classId());
                            if (dto.criterionId() != null) existing.setCriterionId(dto.criterionId());
                            if (dto.pointsValue() != null) existing.setPointsValue(dto.pointsValue());
                            if (dto.dueDate() != null) existing.setDueDate(dto.dueDate());
                            if (dto.scheduledPublishDate() != null) existing.setScheduledPublishDate(dto.scheduledPublishDate());
                            if (dto.scheduledCloseDate() != null) existing.setScheduledCloseDate(dto.scheduledCloseDate());
                            existing.setUpdatedAt(OffsetDateTime.now());
                            return taskUseCase.update(existing);
                        }))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskUseCase.deleteById(id)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> activate(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskUseCase.activate(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }

    public Mono<ServerResponse> deactivate(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskUseCase.deactivate(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> close(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskUseCase.close(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> restore(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskUseCase.restore(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}

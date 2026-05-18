package com.vg.task.web.handler;

import com.vg.task.domain.dto.TaskFilterDTO;
import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.domain.dto.TaskResponseDTO;
import com.vg.task.domain.dto.UpdateTaskRequestDTO;
import com.vg.task.service.TaskService;
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

    private final TaskService taskService;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskService.findAll(), TaskResponseDTO.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskService.findById(id)
                .flatMap(task -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(task))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> findByStatus(ServerRequest request) {
        String status = request.pathVariable("status");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskService.findByStatus(status), TaskResponseDTO.class);
    }

    public Mono<ServerResponse> findByClassId(ServerRequest request) {
        Integer classId = Integer.parseInt(request.pathVariable("classId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskService.findByClassId(classId), TaskResponseDTO.class);
    }

    public Mono<ServerResponse> filter(ServerRequest request) {
        String status = request.queryParam("status").orElse(null);
        String classIdParam = request.queryParam("classId").orElse(null);
        String createdByParam = request.queryParam("createdBy").orElse(null);
        String fromDate = request.queryParam("fromDate").orElse(null);
        String toDate = request.queryParam("toDate").orElse(null);
        
        TaskFilterDTO filter = new TaskFilterDTO(
            status,
            classIdParam != null ? Integer.parseInt(classIdParam) : null,
            createdByParam != null ? Integer.parseInt(createdByParam) : null,
            fromDate != null ? OffsetDateTime.parse(fromDate) : null,
            toDate != null ? OffsetDateTime.parse(toDate) : null,
            false
        );
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskService.filter(filter), TaskResponseDTO.class);
    }

    public Mono<ServerResponse> exportCsv(ServerRequest request) {
        TaskFilterDTO filter = new TaskFilterDTO(null, null, null, null, null, false);
        return taskService.exportToCsv(filter)
                .flatMap(data -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=tasks.csv")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .bodyValue(data));
    }

    public Mono<ServerResponse> exportExcel(ServerRequest request) {
        TaskFilterDTO filter = new TaskFilterDTO(null, null, null, null, null, false);
        return taskService.exportToExcel(filter)
                .flatMap(data -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=tasks.xlsx")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .bodyValue(data));
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(TaskRequestDTO.class)
                .flatMap(taskService::save)
                .flatMap(task -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(task));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        return request.bodyToMono(UpdateTaskRequestDTO.class)
                .flatMap(taskService::update)
                .flatMap(task -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(task))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskService.deleteById(id)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> activate(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskService.activate(id)
                .flatMap(task -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(task))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }

    public Mono<ServerResponse> deactivate(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskService.deactivate(id)
                .flatMap(task -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(task))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }

    public Mono<ServerResponse> close(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskService.close(id)
                .flatMap(task -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(task))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }

    public Mono<ServerResponse> restore(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return taskService.restore(id)
                .flatMap(task -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(task))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage())));
    }
}

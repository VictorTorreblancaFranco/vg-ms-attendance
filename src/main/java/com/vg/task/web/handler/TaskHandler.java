package com.vg.task.web.handler;

import com.vg.task.application.port.input.TaskUseCase;
import com.vg.task.domain.dto.TaskFilterDTO;
import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.domain.dto.TaskResponseDTO;
import com.vg.task.domain.dto.UpdateTaskRequestDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import com.vg.task.mapper.TaskMapper;
import com.vg.task.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tareas", description = "API para gestionar tareas académicas (CRUD, estados, filtros, exportación)")
public class TaskHandler {

    private final TaskUseCase taskUseCase;
    private final TaskMapper mapper;
    private final RateLimitService rateLimitService;

    private Mono<ServerResponse> checkRateLimit(ServerRequest request) {
        String clientIp = request.remoteAddress()
            .map(addr -> addr.getAddress().getHostAddress())
            .orElse("unknown");
        
        return rateLimitService.allowRequest(clientIp)
            .flatMap(allowed -> {
                if (!allowed) {
                    return ServerResponse.status(429)
                        .bodyValue(Map.of(
                            "error", "Too Many Requests",
                            "message", "Has excedido el límite de 10 peticiones por segundo. Por favor espera.",
                            "status", 429
                        ));
                }
                return Mono.empty();
            });
    }

    private Mono<ServerResponse> withRateLimit(ServerRequest request, Mono<ServerResponse> response) {
        return checkRateLimit(request).switchIfEmpty(response);
    }

    @Operation(summary = "Listar todas las tareas", description = "Obtiene una lista de todas las tareas no eliminadas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tareas obtenida exitosamente"),
        @ApiResponse(responseCode = "429", description = "Demasiadas peticiones")
    })
    public Mono<ServerResponse> findAll(ServerRequest request) {
        return withRateLimit(request, ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskUseCase.findAll().map(mapper::toResponse), TaskResponseDTO.class));
    }
    
    @Operation(summary = "Listar tareas paginadas", description = "Obtiene una lista paginada de tareas con metadatos de paginación")
    @Parameters({
        @Parameter(name = "page", description = "Número de página (0-indexed)", example = "0", in = ParameterIn.QUERY),
        @Parameter(name = "size", description = "Tamaño de página", example = "20", in = ParameterIn.QUERY)
    })
    public Mono<ServerResponse> findAllPaged(ServerRequest request) {
        return withRateLimit(request, Mono.defer(() -> {
            int page = Integer.parseInt(request.queryParam("page").orElse("0"));
            int size = Integer.parseInt(request.queryParam("size").orElse("20"));
            return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskUseCase.findAllPaged(page, size)
                    .map(pageResponse -> new PageResponseDTO<>(
                        pageResponse.content().stream().map(mapper::toResponse).toList(),
                        pageResponse.pageNumber(),
                        pageResponse.pageSize(),
                        pageResponse.totalElements(),
                        pageResponse.totalPages(),
                        pageResponse.first(),
                        pageResponse.last()
                    )), PageResponseDTO.class);
        }));
    }

    @Operation(summary = "Buscar tarea por ID", description = "Obtiene los detalles de una tarea específica por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarea encontrada"),
        @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, taskUseCase.findById(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .switchIfEmpty(ServerResponse.notFound().build()));
    }

    @Operation(summary = "Buscar tareas por estado", description = "Filtra tareas por estado (draft, published, closed, archived)")
    public Mono<ServerResponse> findByStatus(ServerRequest request) {
        String status = request.pathVariable("status");
        return withRateLimit(request, ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskUseCase.findByStatus(status).map(mapper::toResponse), TaskResponseDTO.class));
    }

    @Operation(summary = "Buscar tareas por clase", description = "Filtra tareas por ID de clase")
    public Mono<ServerResponse> findByClassId(ServerRequest request) {
        Integer classId = Integer.parseInt(request.pathVariable("classId"));
        return withRateLimit(request, ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskUseCase.findByClassId(classId).map(mapper::toResponse), TaskResponseDTO.class));
    }

    @Operation(summary = "Filtrar tareas", description = "Filtra tareas por múltiples criterios (estado, clase, fechas)")
    public Mono<ServerResponse> filter(ServerRequest request) {
        TaskFilterDTO filter = new TaskFilterDTO(
                request.queryParam("status").orElse(null),
                request.queryParam("classId").map(Integer::parseInt).orElse(null),
                request.queryParam("createdBy").map(Integer::parseInt).orElse(null),
                request.queryParam("fromDate").map(OffsetDateTime::parse).orElse(null),
                request.queryParam("toDate").map(OffsetDateTime::parse).orElse(null),
                false
        );
        return withRateLimit(request, ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskUseCase.filter(filter).map(mapper::toResponse), TaskResponseDTO.class));
    }

    @Operation(summary = "Exportar a CSV", description = "Exporta todas las tareas a un archivo CSV")
    public Mono<ServerResponse> exportCsv(ServerRequest request) {
        TaskFilterDTO filter = new TaskFilterDTO(null, null, null, null, null, false);
        return withRateLimit(request, taskUseCase.exportToCsv(filter)
                .flatMap(data -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=tasks.csv")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .bodyValue(data)));
    }

    @Operation(summary = "Exportar a Excel", description = "Exporta todas las tareas a un archivo Excel")
    public Mono<ServerResponse> exportExcel(ServerRequest request) {
        TaskFilterDTO filter = new TaskFilterDTO(null, null, null, null, null, false);
        return withRateLimit(request, taskUseCase.exportToExcel(filter)
                .flatMap(data -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=tasks.xlsx")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .bodyValue(data)));
    }

    @Operation(summary = "Crear nueva tarea", description = "Crea una nueva tarea en estado borrador")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos de la tarea a crear",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                {
                  "title": "Tarea de Matemáticas",
                  "description": "Resolver ejercicios del capítulo 5",
                  "instructions": "https://docs.google.com/document/d/ejemplo",
                  "classId": 101,
                  "pointsValue": 20.0,
                  "dueDate": "2025-12-31T23:59:59-05:00",
                  "createdBy": 1
                }
                """
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public Mono<ServerResponse> save(ServerRequest request) {
        return withRateLimit(request, request.bodyToMono(TaskRequestDTO.class)
                .map(mapper::toDomain)
                .flatMap(taskUseCase::save)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response)));
    }

    @Operation(summary = "Actualizar tarea", description = "Actualiza los campos de una tarea existente")
    public Mono<ServerResponse> update(ServerRequest request) {
        return withRateLimit(request, request.bodyToMono(UpdateTaskRequestDTO.class)
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
                .switchIfEmpty(ServerResponse.notFound().build()));
    }

    @Operation(summary = "Eliminar tarea", description = "Eliminación lógica de una tarea")
    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, taskUseCase.deleteById(id)
                .then(ServerResponse.noContent().build()));
    }

    @Operation(summary = "Activar/Publicar tarea", description = "Cambia el estado de borrador a publicado")
    public Mono<ServerResponse> activate(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, taskUseCase.activate(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .onErrorResume(IllegalStateException.class, e ->
                        ServerResponse.badRequest().bodyValue(Map.of("error", e.getMessage()))));
    }

    @Operation(summary = "Desactivar/Archivar tarea", description = "Cambia el estado a archivado")
    public Mono<ServerResponse> deactivate(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, taskUseCase.deactivate(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response)));
    }

    @Operation(summary = "Cerrar tarea", description = "Cierra la tarea, no acepta más entregas")
    public Mono<ServerResponse> close(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, taskUseCase.close(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response)));
    }

    @Operation(summary = "Restaurar tarea", description = "Restaura una tarea eliminada lógicamente")
    public Mono<ServerResponse> restore(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return withRateLimit(request, taskUseCase.restore(id)
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok().bodyValue(response)));
    }
}

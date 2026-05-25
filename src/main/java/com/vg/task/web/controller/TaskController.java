package com.vg.task.web.controller;

import com.vg.task.domain.dto.*;
import com.vg.task.mapper.TaskMapper;
import com.vg.task.service.port.TaskUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskUseCase taskUseCase;
    private final TaskMapper taskMapper;

    @GetMapping
    public Flux<TaskResponseDTO> findAll() {
        return taskUseCase.findAll().map(taskMapper::toResponse);
    }

    @GetMapping("/paged")
    public Mono<PageResponseDTO<TaskResponseDTO>> findAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return taskUseCase.findAllPaged(page, size)
                .map(pageResponse -> new PageResponseDTO<>(
                        pageResponse.content().stream().map(taskMapper::toResponse).toList(),
                        pageResponse.pageNumber(), pageResponse.pageSize(),
                        pageResponse.totalElements(), pageResponse.totalPages(),
                        pageResponse.first(), pageResponse.last()
                ));
    }

    @GetMapping("/{id}")
    public Mono<TaskResponseDTO> findById(@PathVariable Long id) {
        return taskUseCase.findById(id).map(taskMapper::toResponse);
    }

    @GetMapping("/status/{status}")
    public Flux<TaskResponseDTO> findByStatus(@PathVariable String status) {
        return taskUseCase.findByStatus(status).map(taskMapper::toResponse);
    }

    @GetMapping("/class/{classId}")
    public Flux<TaskResponseDTO> findByClassId(@PathVariable Integer classId) {
        return taskUseCase.findByClassId(classId).map(taskMapper::toResponse);
    }

    @GetMapping("/filter")
    public Flux<TaskResponseDTO> filter(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer createdBy) {
        TaskFilterDTO filter = new TaskFilterDTO(status, classId, createdBy, null, null, false);
        return taskUseCase.filter(filter).map(taskMapper::toResponse);
    }

    @GetMapping("/overdue")
    public Flux<TaskResponseDTO> findOverdue() {
        return taskUseCase.findOverdueTasks().map(taskMapper::toResponse);
    }

    @GetMapping("/upcoming")
    public Flux<TaskResponseDTO> findUpcoming(@RequestParam(defaultValue = "7") int days) {
        return taskUseCase.findUpcomingTasks(days).map(taskMapper::toResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TaskResponseDTO> save(@Valid @RequestBody TaskRequestDTO request) {
        return taskUseCase.save(taskMapper.toDomain(request)).map(taskMapper::toResponse);
    }

    @PutMapping
    public Mono<TaskResponseDTO> update(@Valid @RequestBody UpdateTaskRequestDTO request) {
        return taskUseCase.findById(request.id())
                .flatMap(existing -> {
                    if (request.title() != null) existing.setTitle(request.title());
                    if (request.description() != null) existing.setDescription(request.description());
                    if (request.instructions() != null) existing.setInstructions(request.instructions());
                    if (request.classId() != null) existing.setClassId(request.classId());
                    if (request.pointsValue() != null) existing.setPointsValue(request.pointsValue());
                    if (request.dueDate() != null) existing.setDueDate(request.dueDate());
                    return taskUseCase.update(existing);
                })
                .map(taskMapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return taskUseCase.deleteById(id);
    }

    @PatchMapping("/{id}/activate")
    public Mono<TaskResponseDTO> activate(@PathVariable Long id) {
        return taskUseCase.activate(id).map(taskMapper::toResponse);
    }

    @PatchMapping("/{id}/deactivate")
    public Mono<TaskResponseDTO> deactivate(@PathVariable Long id) {
        return taskUseCase.deactivate(id).map(taskMapper::toResponse);
    }

    @PatchMapping("/{id}/close")
    public Mono<TaskResponseDTO> close(@PathVariable Long id) {
        return taskUseCase.close(id).map(taskMapper::toResponse);
    }

    @PatchMapping("/{id}/restore")
    public Mono<TaskResponseDTO> restore(@PathVariable Long id) {
        return taskUseCase.restore(id).map(taskMapper::toResponse);
    }

    @GetMapping("/export/csv")
    public Mono<byte[]> exportCsv() {
        return taskUseCase.exportToCsv(new TaskFilterDTO(null, null, null, null, null, false));
    }

    @GetMapping("/export/excel")
    public Mono<byte[]> exportExcel() {
        return taskUseCase.exportToExcel(new TaskFilterDTO(null, null, null, null, null, false));
    }
}

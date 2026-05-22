package com.vg.task.service.impl;

import com.vg.task.application.port.input.TaskUseCase;
import com.vg.task.client.ResilientAcademicClient;
import com.vg.task.domain.model.Task;
import com.vg.task.domain.dto.TaskFilterDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import com.vg.task.exception.BadRequestException;
import com.vg.task.exception.NotFoundException;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskUseCase {
    
    private final TaskRepository taskRepository;
    private final ResilientAcademicClient resilientAcademicClient;
    private final ExportService exportService;
    
    @Override
    public Flux<Task> findAll() {
        return taskRepository.findAll()
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()));
    }
    
    @Override
    public Mono<PageResponseDTO<Task>> findAllPaged(int page, int size) {
        int offset = page * size;
        return taskRepository.findAll()
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .collectList()
                .flatMap(allTasks -> {
                    int total = allTasks.size();
                    int start = Math.min(offset, total);
                    int end = Math.min(offset + size, total);
                    List<Task> pagedTasks = new ArrayList<>();
                    if (start < end) {
                        pagedTasks = allTasks.subList(start, end);
                    }
                    return Mono.just(PageResponseDTO.of(pagedTasks, page, size, total));
                });
    }
    
    @Override
    public Mono<Task> findById(Long id) {
        return taskRepository.findById(id)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + id)));
    }
    
    @Override
    public Flux<Task> findByStatus(String status) {
        return taskRepository.findByStatus(status)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()));
    }
    
    @Override
    public Flux<Task> findByClassId(Integer classId) {
        return taskRepository.findByClassId(classId)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()));
    }
    
    @Override
    public Flux<Task> filter(TaskFilterDTO filter) {
        Flux<Task> query = taskRepository.findAll();
        if (filter.status() != null && !filter.status().isBlank()) {
            query = query.filter(t -> filter.status().equals(t.getStatus()));
        }
        if (filter.classId() != null) {
            query = query.filter(t -> filter.classId().equals(t.getClassId()));
        }
        if (filter.createdBy() != null) {
            query = query.filter(t -> filter.createdBy().equals(t.getCreatedBy()));
        }
        return query.filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()));
    }
    
    @Override
    public Mono<Task> save(Task task) {
        log.info("📝 Creando tarea: {}", task.getTitle());
        
        if (task.getInstructions() == null || task.getInstructions().isBlank()) {
            return Mono.error(new BadRequestException("Debe proporcionar un link o instrucción"));
        }
        if (task.getDueDate() == null) {
            return Mono.error(new BadRequestException("La fecha de entrega es requerida"));
        }
        if (task.getDueDate().isBefore(OffsetDateTime.now())) {
            return Mono.error(new BadRequestException("La fecha de entrega no puede ser en el pasado"));
        }
        
        return resilientAcademicClient.validateClassWithRetry(task.getClassId())
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new BadRequestException("La clase " + task.getClassId() + " no es válida"));
                    }
                    task.setStatus("draft");
                    task.setIsDeleted(false);
                    task.setCreatedAt(OffsetDateTime.now());
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                })
                .doOnSuccess(saved -> log.info("✅ Tarea guardada con ID: {}", saved.getId()))
                .doOnError(error -> log.error("❌ Error: {}", error.getMessage()));
    }
    
    @Override
    public Mono<Task> update(Task task) {
        return taskRepository.findById(task.getId())
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + task.getId())))
                .filter(existing -> !Boolean.TRUE.equals(existing.getIsDeleted()))
                .flatMap(existing -> {
                    if (task.getTitle() != null) existing.setTitle(task.getTitle());
                    if (task.getDescription() != null) existing.setDescription(task.getDescription());
                    if (task.getInstructions() != null) existing.setInstructions(task.getInstructions());
                    if (task.getClassId() != null) existing.setClassId(task.getClassId());
                    if (task.getPointsValue() != null) existing.setPointsValue(task.getPointsValue());
                    if (task.getDueDate() != null) existing.setDueDate(task.getDueDate());
                    existing.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(existing);
                });
    }
    
    @Override
    public Mono<Void> deleteById(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + id)))
                .flatMap(task -> {
                    task.setIsDeleted(true);
                    task.setDeletedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                })
                .then();
    }
    
    @Override
    public Mono<Task> activate(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + id)))
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .flatMap(task -> {
                    if (!"draft".equals(task.getStatus())) {
                        return Mono.error(new BadRequestException("Solo tareas en borrador pueden publicarse"));
                    }
                    task.setStatus("published");
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                });
    }
    
    @Override
    public Mono<Task> deactivate(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + id)))
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .flatMap(task -> {
                    task.setStatus("archived");
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                });
    }
    
    @Override
    public Mono<Task> close(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + id)))
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .flatMap(task -> {
                    task.setStatus("closed");
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                });
    }
    
    @Override
    public Mono<Task> restore(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + id)))
                .flatMap(task -> {
                    task.setIsDeleted(false);
                    task.setDeletedAt(null);
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                });
    }
    
    @Override
    public Mono<byte[]> exportToCsv(TaskFilterDTO filter) {
        log.info("📊 Exportando tareas a CSV");
        return filter(filter)
            .collectList()
            .flatMap(exportService::exportToCsv);
    }
    
    @Override
    public Mono<byte[]> exportToExcel(TaskFilterDTO filter) {
        log.info("📊 Exportando tareas a Excel");
        return filter(filter)
            .collectList()
            .flatMap(exportService::exportToExcel);
    }
}

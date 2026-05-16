package com.vg.task.service.impl;

import com.vg.task.client.AcademicClient;
import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.domain.dto.TaskResponseDTO;
import com.vg.task.domain.model.Task;
import com.vg.task.exception.NotFoundException;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final AcademicClient academicClient;

    @Override
    public Flux<TaskResponseDTO> findAll() {
        return taskRepository.findAll()
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<TaskResponseDTO> findById(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<TaskResponseDTO> create(TaskRequestDTO request) {
        return academicClient.validateClass(request.classId())
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new NotFoundException("Class not found with id: " + request.classId()));
                    }
                    
                    Task task = Task.builder()
                            .classId(request.classId())
                            .criterionId(request.criterionId())
                            .title(request.title())
                            .description(request.description())
                            .instructions(request.instructions())
                            .dueDate(request.dueDate())
                            .pointsValue(request.pointsValue() != null ? request.pointsValue() : 0.0)
                            .allowedAttempts(request.allowedAttempts() != null ? request.allowedAttempts() : 1)
                            .isGroupTask(request.isGroupTask() != null ? request.isGroupTask() : false)
                            .visibleToParents(request.visibleToParents() != null ? request.visibleToParents() : true)
                            .assignmentDate(LocalDate.now())
                            .status("draft")
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    
                    return taskRepository.save(task);
                })
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<TaskResponseDTO> update(Long id, TaskRequestDTO request) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(existingTask -> academicClient.validateClass(request.classId())
                        .flatMap(isValid -> {
                            if (!isValid) {
                                return Mono.error(new NotFoundException("Class not found with id: " + request.classId()));
                            }
                            
                            existingTask.setClassId(request.classId());
                            existingTask.setCriterionId(request.criterionId());
                            existingTask.setTitle(request.title());
                            existingTask.setDescription(request.description());
                            existingTask.setInstructions(request.instructions());
                            existingTask.setDueDate(request.dueDate());
                            existingTask.setPointsValue(request.pointsValue() != null ? request.pointsValue() : 0.0);
                            existingTask.setAllowedAttempts(request.allowedAttempts() != null ? request.allowedAttempts() : 1);
                            existingTask.setIsGroupTask(request.isGroupTask() != null ? request.isGroupTask() : false);
                            existingTask.setVisibleToParents(request.visibleToParents() != null ? request.visibleToParents() : true);
                            existingTask.setUpdatedAt(OffsetDateTime.now());
                            
                            return taskRepository.save(existingTask);
                        }))
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(taskRepository::delete);
    }

    @Override
    public Flux<TaskResponseDTO> findByClassId(Integer classId) {
        return taskRepository.findByClassId(classId)
                .map(this::toResponseDTO);
    }

    @Override
    public Flux<TaskResponseDTO> findByStatus(String status) {
        return taskRepository.findByStatus(status)
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<TaskResponseDTO> publish(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(task -> {
                    if (!"draft".equals(task.getStatus())) {
                        return Mono.error(new IllegalStateException("Only draft tasks can be published. Current status: " + task.getStatus()));
                    }
                    task.setStatus("published");
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                })
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<TaskResponseDTO> close(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(task -> {
                    if (!"published".equals(task.getStatus())) {
                        return Mono.error(new IllegalStateException("Only published tasks can be closed. Current status: " + task.getStatus()));
                    }
                    task.setStatus("closed");
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                })
                .map(this::toResponseDTO);
    }

    private TaskResponseDTO toResponseDTO(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getClassId(),
                task.getCriterionId(),
                task.getTitle(),
                task.getDescription(),
                task.getInstructions(),
                task.getAssignmentDate(),
                task.getDueDate(),
                task.getPointsValue(),
                task.getAllowedAttempts(),
                task.getIsGroupTask(),
                task.getVisibleToParents(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}

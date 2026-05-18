package com.vg.task.service.impl;

import com.vg.task.client.AcademicClient;
import com.vg.task.domain.dto.TaskFileDTO;
import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.domain.dto.TaskResponseDTO;
import com.vg.task.domain.dto.UpdateTaskRequestDTO;
import com.vg.task.domain.model.Task;
import com.vg.task.domain.model.TaskFile;
import com.vg.task.exception.BadRequestException;
import com.vg.task.exception.NotFoundException;
import com.vg.task.repository.TaskFileRepository;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.NotificationService;
import com.vg.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final int MAX_FILES = 5;
    private static final int MIN_FILES = 1;

    private final TaskRepository taskRepository;
    private final TaskFileRepository taskFileRepository;
    private final AcademicClient academicClient;
    private final NotificationService notificationService;

    @Override
    public Flux<TaskResponseDTO> findAll() {
        return taskRepository.findAll()
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Flux<TaskResponseDTO> findByStatus(String status) {
        return taskRepository.findByStatus(status)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Flux<TaskResponseDTO> findByClassId(Integer classId) {
        return taskRepository.findByClassId(classId)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Mono<TaskResponseDTO> findById(Long id) {
        return taskRepository.findById(id)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Mono<TaskResponseDTO> save(TaskRequestDTO request) {
        // Validar archivos
        if (request.files() == null || request.files().isEmpty()) {
            return Mono.error(new BadRequestException("At least one file or link is required"));
        }
        if (request.files().size() > MAX_FILES) {
            return Mono.error(new BadRequestException("Maximum " + MAX_FILES + " files allowed, got " + request.files().size()));
        }
        
        return academicClient.validateClass(request.classId())
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new NotFoundException("Class not found with id: " + request.classId()));
                    }
                    
                    OffsetDateTime now = OffsetDateTime.now();
                    
                    Task task = Task.builder()
                            .title(request.title())
                            .description(request.description())
                            .instructions(request.instructions())
                            .classId(request.classId())
                            .criterionId(request.criterionId())
                            .pointsValue(request.pointsValue() != null ? request.pointsValue() : 0.0)
                            .dueDate(request.dueDate())
                            .scheduledPublishDate(request.scheduledPublishDate())
                            .scheduledCloseDate(request.scheduledCloseDate())
                            .status("draft")
                            .isDeleted(false)
                            .createdBy(request.createdBy())
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    
                    return taskRepository.save(task)
                            .flatMap(savedTask -> {
                                if (request.files() != null && !request.files().isEmpty()) {
                                    List<TaskFile> files = new ArrayList<>();
                                    for (TaskFileDTO fileDTO : request.files()) {
                                        TaskFile file = TaskFile.builder()
                                                .taskId(savedTask.getId())
                                                .fileName(fileDTO.fileName())
                                                .fileUrl(fileDTO.fileUrl())
                                                .fileType(fileDTO.fileType())
                                                .fileSizeKb(fileDTO.fileSizeKb())
                                                .createdAt(now)
                                                .build();
                                        files.add(file);
                                    }
                                    return taskFileRepository.saveAll(files)
                                            .collectList()
                                            .thenReturn(savedTask);
                                }
                                return Mono.just(savedTask);
                            });
                })
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Mono<TaskResponseDTO> update(UpdateTaskRequestDTO request) {
        return taskRepository.findById(request.id())
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + request.id())))
                .flatMap(task -> {
                    if (request.title() != null) task.setTitle(request.title());
                    if (request.description() != null) task.setDescription(request.description());
                    if (request.instructions() != null) task.setInstructions(request.instructions());
                    if (request.classId() != null) task.setClassId(request.classId());
                    if (request.criterionId() != null) task.setCriterionId(request.criterionId());
                    if (request.pointsValue() != null) task.setPointsValue(request.pointsValue());
                    if (request.dueDate() != null) task.setDueDate(request.dueDate());
                    if (request.scheduledPublishDate() != null) task.setScheduledPublishDate(request.scheduledPublishDate());
                    if (request.scheduledCloseDate() != null) task.setScheduledCloseDate(request.scheduledCloseDate());
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                })
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return taskRepository.findById(id)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(task -> {
                    task.setIsDeleted(true);
                    task.setDeletedAt(OffsetDateTime.now());
                    return taskRepository.save(task).then();
                });
    }

    @Override
    public Mono<TaskResponseDTO> activate(Long id) {
        return taskRepository.findById(id)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(task -> {
                    task.setStatus("published");
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task)
                            .flatMap(saved -> notificationService.send(
                                    saved.getCreatedBy(),
                                    saved.getId(),
                                    "TASK_PUBLISHED",
                                    "Tarea Publicada",
                                    "La tarea '" + saved.getTitle() + "' ya está disponible para los estudiantes"
                            ).thenReturn(saved));
                })
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Mono<TaskResponseDTO> deactivate(Long id) {
        return taskRepository.findById(id)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(task -> {
                    task.setStatus("archived");
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                })
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Mono<TaskResponseDTO> close(Long id) {
        return taskRepository.findById(id)
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(task -> {
                    task.setStatus("closed");
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task)
                            .flatMap(saved -> notificationService.send(
                                    saved.getCreatedBy(),
                                    saved.getId(),
                                    "TASK_CLOSED",
                                    "Tarea Cerrada",
                                    "La tarea '" + saved.getTitle() + "' ha sido cerrada"
                            ).thenReturn(saved));
                })
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Mono<TaskResponseDTO> restore(Long id) {
        return taskRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + id)))
                .flatMap(task -> {
                    task.setIsDeleted(false);
                    task.setDeletedAt(null);
                    task.setUpdatedAt(OffsetDateTime.now());
                    return taskRepository.save(task);
                })
                .flatMap(this::toResponseWithFiles);
    }

    private Mono<TaskResponseDTO> toResponseWithFiles(Task task) {
        return taskFileRepository.findByTaskId(task.getId())
                .map(file -> new TaskFileDTO(
                        file.getId(),
                        file.getFileName(),
                        file.getFileUrl(),
                        file.getFileType(),
                        file.getFileSizeKb()
                ))
                .collectList()
                .map(files -> new TaskResponseDTO(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getInstructions(),
                        task.getClassId(),
                        task.getCriterionId(),
                        task.getPointsValue(),
                        task.getDueDate(),
                        task.getScheduledPublishDate(),
                        task.getScheduledCloseDate(),
                        task.getStatus(),
                        task.getIsDeleted(),
                        task.getCreatedBy(),
                        task.getCreatedAt(),
                        task.getUpdatedAt(),
                        task.getDeletedAt(),
                        files
                ));
    }
}

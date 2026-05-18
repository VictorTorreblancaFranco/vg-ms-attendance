package com.vg.task.service.impl;

import com.opencsv.CSVWriter;
import com.vg.task.client.AcademicClient;
import com.vg.task.domain.dto.TaskFileDTO;
import com.vg.task.domain.dto.TaskFilterDTO;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Flux<TaskResponseDTO> findAll() {
        return taskRepository.findAll()
                .filter(task -> !Boolean.TRUE.equals(task.getIsDeleted()))
                .flatMap(this::toResponseWithFiles);
    }

    @Override
    public Flux<TaskResponseDTO> filter(TaskFilterDTO filter) {
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
        if (filter.fromDate() != null) {
            query = query.filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(filter.fromDate()));
        }
        if (filter.toDate() != null) {
            query = query.filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isBefore(filter.toDate()));
        }
        if (filter.isDeleted() != null) {
            query = query.filter(t -> filter.isDeleted().equals(t.getIsDeleted()));
        } else {
            query = query.filter(t -> !Boolean.TRUE.equals(t.getIsDeleted()));
        }
        
        return query.flatMap(this::toResponseWithFiles);
    }

    @Override
    public Mono<byte[]> exportToCsv(TaskFilterDTO filter) {
        return filter(filter)
                .collectList()
                .map(tasks -> {
                    try (StringWriter stringWriter = new StringWriter();
                         CSVWriter csvWriter = new CSVWriter(stringWriter)) {
                        
                        csvWriter.writeNext(new String[]{"ID", "Title", "Status", "Class ID", "Points", "Due Date", "Created At", "Files"});
                        
                        for (TaskResponseDTO task : tasks) {
                            csvWriter.writeNext(new String[]{
                                    String.valueOf(task.id()),
                                    task.title(),
                                    task.status(),
                                    String.valueOf(task.classId()),
                                    String.valueOf(task.pointsValue()),
                                    task.dueDate() != null ? task.dueDate().format(DATE_FORMATTER) : "",
                                    task.createdAt() != null ? task.createdAt().format(DATE_FORMATTER) : "",
                                    task.files() != null ? String.valueOf(task.files().size()) : "0"
                            });
                        }
                        
                        return stringWriter.toString().getBytes();
                    } catch (IOException e) {
                        throw new RuntimeException("Error generating CSV", e);
                    }
                });
    }

    @Override
    public Mono<byte[]> exportToExcel(TaskFilterDTO filter) {
        return filter(filter)
                .collectList()
                .map(tasks -> {
                    try (Workbook workbook = new XSSFWorkbook();
                         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                        
                        Sheet sheet = workbook.createSheet("Tasks");
                        
                        Row headerRow = sheet.createRow(0);
                        String[] headers = {"ID", "Title", "Status", "Class ID", "Points", "Due Date", "Created At", "Files"};
                        CellStyle headerStyle = getHeaderStyle(workbook);
                        for (int i = 0; i < headers.length; i++) {
                            Cell cell = headerRow.createCell(i);
                            cell.setCellValue(headers[i]);
                            cell.setCellStyle(headerStyle);
                        }
                        
                        int rowNum = 1;
                        for (TaskResponseDTO task : tasks) {
                            Row row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(task.id());
                            row.createCell(1).setCellValue(task.title());
                            row.createCell(2).setCellValue(task.status());
                            row.createCell(3).setCellValue(task.classId());
                            row.createCell(4).setCellValue(task.pointsValue());
                            row.createCell(5).setCellValue(task.dueDate() != null ? task.dueDate().format(DATE_FORMATTER) : "");
                            row.createCell(6).setCellValue(task.createdAt() != null ? task.createdAt().format(DATE_FORMATTER) : "");
                            row.createCell(7).setCellValue(task.files() != null ? task.files().size() : 0);
                        }
                        
                        for (int i = 0; i < headers.length; i++) {
                            sheet.autoSizeColumn(i);
                        }
                        
                        workbook.write(outputStream);
                        return outputStream.toByteArray();
                    } catch (IOException e) {
                        throw new RuntimeException("Error generating Excel", e);
                    }
                });
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
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
                                    "La tarea '" + saved.getTitle() + "' ya está disponible"
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
